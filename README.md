# Expert [![Build Status](http://6713d71a.ngrok.io/jenkins/buildStatus/icon?job=DAQExpert&build=3)](http://6713d71a.ngrok.io/jenkins/job/DAQExpert/3/)
New expert system processing data model produced by DAQAggregator

Current version of DAQExpert can be accessed at http://daq-expert.cern.ch/

## Results overview

Expert identifies known problems described in [Flowchart](https://twiki.cern.ch/twiki/pub/CMS/ShiftNews/DAQStuck3.pdf). DAQ snapshots are persisted since 2016-05-29, since then following cases has beed identified.

##### Flowchart case 1:
http://dev-daq-expert.cern.ch/?start=2016-05-31T12:26&end=2016-05-31T12:33

Note that cases before 2016-06-02 are based on incomplete snapshots thus may miss some details.

##### Flowchart case 2:
Not available (spotlight access necessary)

##### Flowchart case 3:
http://dev-daq-expert.cern.ch/?start=2016-06-20T12:50&end=2016-06-20T12:56

##### Flowchart case 4:
no case yet

##### Flowchart case 5:
http://dev-daq-expert.cern.ch/?start=2016-06-26T18:07&end=2016-06-26T18:16

Note that ~2 minutes of stable beams was lost due to wrong decision.

##### Flowchart case 6:
http://dev-daq-expert.cern.ch/?start=2016-06-25T21:41&end=2016-06-25T21:43


##### Known issues:
Data not persisted for few days due to tunnel fail (partly during STABLE BEAMS):
http://dev-daq-expert.cern.ch/?start=2016-06-16&end=2016-06-21

Data not persisted for 20h due to DAQAggregator fail (during NO BEAM):
http://dev-daq-expert.cern.ch/?start=2016-06-08T04:00&end=2016-06-09T12:00

Rate overflow before 29 June