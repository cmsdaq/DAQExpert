package rcms.utilities.daqexpert.jobs;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.h2.tools.Recover;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.reasoning.base.action.Action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RecoveryBuilder {


    private static final Logger logger = Logger.getLogger(RecoveryBuilder.class);

    public List<RecoveryRequest> getRecoveries(List<String> steps, String problemDescription) {

        if(steps == null || steps.size() == 0){
            return new ArrayList<>();
        }

        logger.debug("Building jobs from action steps " + steps);

        List<List<Pair<Jobs,String>>> stepsOfJobs = getJobs(steps);

        List<RecoveryRequest> requests = new ArrayList<>();

        for (List<Pair<Jobs,String>> jobs : stepsOfJobs) {

            for (Pair<Jobs,String> job : jobs) {

                RecoveryRequest recoveryRequest = new RecoveryRequest();
                recoveryRequest.setRedRecycle(new HashSet<>());
                recoveryRequest.setGreenRecycle(new HashSet<>());
                recoveryRequest.setReset(new HashSet<>());
                recoveryRequest.setFault(new HashSet<>());
                recoveryRequest.setProblemDescription(problemDescription);

                Jobs recoverJob = job.getLeft();
                String subsystem = job.getRight();

                switch (recoverJob) {
                    case RedRecycle:
                        recoveryRequest.getRedRecycle().add(subsystem);
                        break;
                    case GreenRecycle:
                        recoveryRequest.getGreenRecycle().add(subsystem);
                        break;
                    default:
                        break;
                }

                requests.add(recoveryRequest);

            }
        }

        return requests;
    }

    public List<List<Pair<Jobs,String>>> getJobs(List<String> steps) {

        List<List<Pair<Jobs,String>>> stepsOfJobs = new ArrayList<>();

        for (String step : steps) {

            List<Pair<Jobs,String>> jobs = getJobsFromStep(step);

            logger.trace("JOBS: " + jobs);
            stepsOfJobs.add(jobs);
        }

        return stepsOfJobs;
    }

    private List<Pair<Jobs,String> > getJobsFromStep(String step) {
        List<Pair<Jobs,String>> jobs = new ArrayList<>();
        Pattern pattern = Pattern.compile(".*\\<\\<\\w*\\:\\:\\w*\\>\\>.*");
        Matcher matcher = pattern.matcher(step);


        int startIndex = 0;
        while (matcher.matches()) {

            int start = step.indexOf("<<", startIndex);
            int delimiter = step.indexOf("::", startIndex);

            int end = step.indexOf(">>", start);
            String operation = step.substring(start + 2, delimiter);
            String subsystem = step.substring(delimiter + 2, end);
            logger.info("Found runnable step " + operation + " to subsystem "+ subsystem+" in " + step);

            jobs.add( Pair.of( Jobs.valueOf(operation), subsystem ) ) ;

            matcher = pattern.matcher(step.substring(end, step.length()));
            startIndex = end;

        }
        return jobs;
    }

}
