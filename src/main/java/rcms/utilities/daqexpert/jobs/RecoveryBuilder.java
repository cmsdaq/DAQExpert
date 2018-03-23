package rcms.utilities.daqexpert.jobs;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.h2.tools.Recover;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.reasoning.base.action.Action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class RecoveryBuilder {


    private static final Logger logger = Logger.getLogger(RecoveryBuilder.class);

    public List<RecoveryRequest> getRecoveries(List<String> steps, String problemDescription, Long problemId) {

        if(steps == null || steps.size() == 0){
            return new ArrayList<>();
        }

        logger.debug("Building jobs from action steps " + steps);

        List<List<Pair<Jobs,List<String>>>> stepsOfJobs = getJobs(steps);

        List<RecoveryRequest> requests = new ArrayList<>();

        for (List<Pair<Jobs,List<String>>> jobs : stepsOfJobs) {

            for (Pair<Jobs,List<String>> job : jobs) {

                RecoveryRequest recoveryRequest = new RecoveryRequest();
                recoveryRequest.setRedRecycle(new HashSet<>());
                recoveryRequest.setGreenRecycle(new HashSet<>());
                recoveryRequest.setReset(new HashSet<>());
                recoveryRequest.setFault(new HashSet<>());
                recoveryRequest.setProblemDescription(problemDescription);
                recoveryRequest.setProblemId(problemId);

                Jobs recoverJob = job.getLeft();
                List<String> subsystems = job.getRight();

                switch (recoverJob) {
                    case RedRecycle:
                        subsystems.forEach(s-> recoveryRequest.getRedRecycle().add(s));
                        break;
                    case GreenRecycle:
                        subsystems.forEach(s-> recoveryRequest.getGreenRecycle().add(s));
                        break;
                    default:
                        break;
                }

                requests.add(recoveryRequest);

            }
        }

        return requests;
    }

    public List<List<Pair<Jobs,List<String>>>> getJobs(List<String> steps) {

        List<List<Pair<Jobs,List<String>>>> stepsOfJobs = new ArrayList<>();

        for (String step : steps) {

            List<Pair<Jobs,List<String>>> jobs = getJobsFromStep(step);

            logger.trace("JOBS: " + jobs);
            stepsOfJobs.add(jobs);
        }

        return stepsOfJobs;
    }

    private List<Pair<Jobs,List<String>> > getJobsFromStep(String step) {
        List<Pair<Jobs,List<String>>> jobs = new ArrayList<>();
        Pattern pattern = Pattern.compile(".*\\<\\<[\\w]*\\:\\:[\\w\\]\\[\\,\\s]*\\>\\>.*");
        Matcher matcher = pattern.matcher(step);


        int startIndex = 0;
        while (matcher.matches()) {

            int start = step.indexOf("<<", startIndex);
            int delimiter = step.indexOf("::", startIndex);

            int end = step.indexOf(">>", start);
            String operation = step.substring(start + 2, delimiter);
            String subsystem = step.substring(delimiter + 2, end);

            List<String> subsystems = new ArrayList<>();
            if(subsystem.startsWith("[") && subsystem.endsWith("]")){
                String listOfSubsystems = subsystem.substring(1, subsystem.length()-1);
                String[] splited = listOfSubsystems.split(",");

                subsystems.addAll(Arrays.stream(splited).map(s->s.trim()).collect(Collectors.toList()));
            } else{
                subsystems.add(subsystem);
            }

            logger.info("Found runnable step " + operation + " to subsystem "+ subsystem+" in " + step);

            jobs.add( Pair.of( Jobs.valueOf(operation), subsystems ) ) ;

            matcher = pattern.matcher(step.substring(end, step.length()));
            startIndex = end;

        }
        return jobs;
    }

}
