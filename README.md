# Expert system 

DAQExpert is an expert system used in the CMS control room to assist the shifters and automate their chores.

## Goal

Main motivation is to increase CMS data-taking efficiency.
DAQExpert aims to reduce the human error in the operations and minimise the on-call expert demand.

## Provided service

It assists the shift crew and the system experts in recovering from operational faults, streamlining the post mortem analysis and, at the end of Run 2, triggering fully automatic recovery without human intervention. 

DAQ Expert analyses the real-time monitoring data originating from the DAQ components and the high-level trigger updated every few seconds.
It pinpoints data flow problems, and recovers them automatically or after given operator approval.

It delivers dataflow analysis results in the form `what's the problem` + `what's the best action to take`.

## System architecture

DAQExpert is one of several services of the CMS expert system.

The input data for the DAQExpert is provided by the DAQAggregator.

The Dashboards - the main Control Room GUI presenting output of the DAQExpert - is provided by the NotificationManager.

For the overview of the entire expert system architecture refer to [link to expert system architecture README].


## Existing deployments

Production deployment of DAQExpert can be accessed within CMS network at http://daq-expert.cms/. Develpment deployment at http://dev-daq-expert.cms/.

## Publications

Several papers on the DAQExpert service and its operational experience have been published.

- [CHEP 2019 - DAQExpert the service to increase CMS data-taking efficiency
](https://www.epj-conferences.org/articles/epjconf/abs/2020/21/epjconf_chep2020_01028/epjconf_chep2020_01028.html)
- [ACAT 2017 - DAQExpert - An expert system to increase CMS data-taking efficiency](https://iopscience.iop.org/article/10.1088/1742-6596/1085/3/032021)



# Contribution

You can contribute to the DAQExpert service itself or to its expert logic.

## Contribution to expert logic

The expert's logic is set of Logic Modules. To contribute add or modify a Logic Module.

### Logic module

Logic Module (abbr. LM) is a building block of DAQExpert expert logic.
- Each LM represents one piece of expert knowledge
- Each LM defines one condition
- The definition of condition is placed in satisfy method
- The method returns true if condition is satisfied and false otherwise
- One LM can use results of another LM

Logic Module allows you to focus on one thing: expressing your knowledge about DAQ system operation.
- Easily access DAQ snapshot
- Perform your checks on DAQ snapshot

Just return true when your condition is satisfied, and the framework will do the rest (visualize, generate notifications, persist results)
