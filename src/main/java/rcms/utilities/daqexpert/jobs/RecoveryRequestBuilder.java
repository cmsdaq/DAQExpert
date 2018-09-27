package rcms.utilities.daqexpert.jobs;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Responsible for preparing recovery requests
 */
public class RecoveryRequestBuilder {


    private static final Logger logger = Logger.getLogger(RecoveryRequestBuilder.class);


    public RecoveryRequest buildRecoveryRequest(List<String> rawSteps, List<String> humanReadableSteps, String title, String problemDescription, Long problemId) {
        return buildRecoveryRequest(rawSteps,humanReadableSteps,title,problemDescription,problemId, new HashSet<>());
    }

    public RecoveryRequest buildRecoveryRequest(List<String> rawSteps, String title, String problemDescription, Long problemId) {
        return buildRecoveryRequest(rawSteps, rawSteps, title, problemDescription, problemId);
    }

    /**
     * Build recovery request from given recovery rawSteps
     *
     * @return recovery request
     */
    public RecoveryRequest buildRecoveryRequest(List<String> rawSteps, List<String> humanReadableSteps, String title, String problemDescription, Long problemId, Set<String> causingSubsystems) {

        if (rawSteps == null || rawSteps.size() == 0) {
            return null;
        }

        logger.debug("Building jobs from action rawSteps " + rawSteps);

        List<List<Pair<RecoveryJob, List<String>>>> stepsOfJobs = getJobs(rawSteps);

        RecoveryRequest recoveryRequest = new RecoveryRequest();
        recoveryRequest.setProblemDescription(problemDescription);
        recoveryRequest.setProblemId(problemId);
        recoveryRequest.setProblemTitle(title);
        recoveryRequest.setRecoverySteps(new ArrayList<RecoveryStep>());

        for (List<Pair<RecoveryJob, List<String>>> jobs : stepsOfJobs) {

            RecoveryStep recoveryStep = new RecoveryStep();
            recoveryStep.setRedRecycle(new HashSet<>());
            recoveryStep.setGreenRecycle(new HashSet<>());
            recoveryStep.setReset(new HashSet<>());
            recoveryStep.setFault(new HashSet<>());
            recoveryStep.setIssueTTCHardReset(false);

            recoveryStep.setStepIndex(stepsOfJobs.indexOf(jobs));
            recoveryStep.setHumanReadable(humanReadableSteps.get(recoveryStep.getStepIndex()));
            boolean enableAutomaticRecoveryForStep = false;

            for (Pair<RecoveryJob, List<String>> job : jobs) {

                RecoveryJob recoverJob = job.getLeft();
                List<String> subsystems = job.getRight();

                switch (recoverJob) {
                    // This step is equivalent to R&G recycle and should be deleted
                    case RedRecycle:
                        subsystems.forEach(s -> recoveryStep.getRedRecycle().add(s));
                        subsystems.forEach(s -> recoveryStep.getGreenRecycle().add(s));
                        subsystems.forEach(s -> recoveryStep.getFault().add(s));
                        enableAutomaticRecoveryForStep = true;
                        break;
                    case GreenRecycle:
                        subsystems.forEach(s -> recoveryStep.getGreenRecycle().add(s));
                        subsystems.forEach(s -> recoveryStep.getFault().add(s));
                        enableAutomaticRecoveryForStep = true;
                        break;
                    case RedAndGreenRecycle:
                        subsystems.forEach(s -> recoveryStep.getGreenRecycle().add(s));
                        subsystems.forEach(s -> recoveryStep.getRedRecycle().add(s));
                        subsystems.forEach(s -> recoveryStep.getFault().add(s));
                        enableAutomaticRecoveryForStep = true;
                        break;
                    case StopAndStartTheRun:
                        causingSubsystems.forEach(s->recoveryStep.getFault().add(s));
                        enableAutomaticRecoveryForStep = true;
                        break;
                    case TTCHardReset:
                        recoveryStep.setIssueTTCHardReset(true);
                        causingSubsystems.forEach(s->recoveryStep.getFault().add(s));
                        enableAutomaticRecoveryForStep = true;
                    default:
                        break;
                }

            }
            if (enableAutomaticRecoveryForStep) {
                recoveryRequest.getRecoverySteps().add(recoveryStep);
            }
        }

        return recoveryRequest;
    }

    /**
     * Extract recovery jobs from given steps
     *
     * @param steps recovery steps
     * @return list of jobs with context for each step
     */
    public List<List<Pair<RecoveryJob, List<String>>>> getJobs(List<String> steps) {

        List<List<Pair<RecoveryJob, List<String>>>> stepsOfJobs = new ArrayList<>();

        for (String step : steps) {

            List<Pair<RecoveryJob, List<String>>> jobs = getJobsFromStep(step);

            logger.trace("JOBS: " + jobs);
            stepsOfJobs.add(jobs);
        }

        return stepsOfJobs;
    }

    /**
     * Extract job and its context from given recovery step
     *
     * @param step recovery step
     * @return list of jobs with context
     */
    private List<Pair<RecoveryJob, List<String>>> getJobsFromStep(String step) {
        List<Pair<RecoveryJob, List<String>>> jobs = new ArrayList<>();
        Pattern pattern = Pattern.compile(".*\\<\\<[\\w]*\\:?\\:?[\\w\\]\\[\\,\\s]*\\>\\>.*");
        Matcher matcher = pattern.matcher(step);

        logger.trace("Getting jobs from step: " + step);

        while (matcher.matches()) {

            int start = step.indexOf("<<");
            int delimiter = step.indexOf("::",start);

            int end = step.indexOf(">>", start);

            List<String> subsystems = new ArrayList<>();
            String operation = null;
            if (delimiter > 0 && delimiter < end) {
                operation = step.substring(start + 2, delimiter);
                logger.info("Delimiters: start:" + start + ", end:" + end + ", delimiter:" + delimiter);
                String subsystem = step.substring(delimiter + 2, end);
                if (subsystem.startsWith("[") && subsystem.endsWith("]")) {
                    String listOfSubsystems = subsystem.substring(1, subsystem.length() - 1);
                    String[] splited = listOfSubsystems.split(",");

                    subsystems.addAll(Arrays.stream(splited).map(s -> s.trim()).collect(Collectors.toList()));
                } else {
                    subsystems.add(subsystem);
                }
            } else {
                operation = step.substring(start + 2, end);
            }

            logger.info("Found runnable step " + operation + " to subsystem " + subsystems + " in " + step);

            jobs.add(Pair.of(RecoveryJob.valueOf(operation), subsystems));

            step = step.substring(end + 2, step.length());
            matcher = pattern.matcher(step);

        }
        return jobs;
    }

}
