package rcms.utilities.daqexpert.reasoning.logic.failures.backpressure;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;

import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.processing.context.SimpleContextEntry;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;

public class RuStuckWaitingTest extends FlowchartCaseTestBase {

	/*
	 * Sat, 01 Apr 2017 17:55:52 GMT
	 * 
	 * http://daq-expert.cms/daq2view-react/index.html?setup=cdaq&time=2017-04-
	 * 01-19:55:52
	 */
	@Test
	public void test() throws URISyntaxException {
		//Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1491069352157.smile");

		assertOnlyOneIsSatisified(ruStuckWaiting, snapshot);

		/* Assert problem FEDs */
		SimpleContextEntry<Integer> problemFeds = (SimpleContextEntry<Integer>) ruStuckWaiting.getContextHandler().getContext().getContextEntryMap().get("PROBLEM-FED");
		SimpleContextEntry<Integer> problemPartitions = (SimpleContextEntry<Integer>) ruStuckWaiting.getContextHandler().getContext().getContextEntryMap().get("PROBLEM-TTCP");
		SimpleContextEntry<Integer> problemSubsystems = (SimpleContextEntry<Integer>) ruStuckWaiting.getContextHandler().getContext().getContextEntryMap().get("PROBLEM-SUBSYSTEM");
		SimpleContextEntry<Integer> problemFedBuilder = (SimpleContextEntry<Integer>) ruStuckWaiting.getContextHandler().getContext().getContextEntryMap().get("PROBLEM-FED-BUILDER");

		assertEquals(8, problemFeds.getObjectSet().size());
		assertThat(problemFeds.getObjectSet(), hasItem(724));
		assertThat(problemFeds.getObjectSet(), hasItem(725));
		assertThat(problemFeds.getObjectSet(), hasItem(726));
		assertThat(problemFeds.getObjectSet(), hasItem(727));
		assertThat(problemFeds.getObjectSet(), hasItem(728));
		assertThat(problemFeds.getObjectSet(), hasItem(729));
		assertThat(problemFeds.getObjectSet(), hasItem(730));
		assertThat(problemFeds.getObjectSet(), hasItem(731));

		/* Assert problem partition and subsystem */
		assertEquals(1, problemPartitions.getObjectSet().size());
		assertEquals("HO", problemPartitions.getObjectSet().iterator().next());
		assertEquals(1, problemSubsystems.getObjectSet().size());
		assertEquals("HCAL", problemSubsystems.getObjectSet().iterator().next());

		/* Assert problem fedBuilder */
		assertEquals(1, problemFedBuilder.getObjectSet().size());
		assertEquals("HOSCAL", problemFedBuilder.getObjectSet().iterator().next());


		SimpleContextEntry<Long> minFragmentCount = (SimpleContextEntry<Long>) ruStuckWaiting.getContextHandler().getContext().getContextEntryMap().get("MIN-FRAGMENT-COUNT");
		SimpleContextEntry<Long> maxFragmentCount = (SimpleContextEntry<Long>) ruStuckWaiting.getContextHandler().getContext().getContextEntryMap().get("MAX-FRAGMENT-COUNT");
		SimpleContextEntry<Long> minFragmentPartition = (SimpleContextEntry<Long>) ruStuckWaiting.getContextHandler().getContext().getContextEntryMap().get("MIN-FRAGMENT-PARTITION");
		SimpleContextEntry<Long> maxFragmentPartition = (SimpleContextEntry<Long>) ruStuckWaiting.getContextHandler().getContext().getContextEntryMap().get("MAX-FRAGMENT-PARTITION");
		/* Assert trigger count info */
		assertEquals(1, minFragmentCount.getObjectSet().size());
		assertEquals(1, maxFragmentCount.getObjectSet().size());
		assertEquals(1, minFragmentPartition.getObjectSet().size());
		assertEquals(1, maxFragmentPartition.getObjectSet().size());
		assertEquals(4731L, maxFragmentCount.getObjectSet().iterator().next().longValue());
		assertEquals(0L, minFragmentCount.getObjectSet().iterator().next().longValue());
		assertEquals("SCAL", maxFragmentPartition.getObjectSet().iterator().next());
		assertEquals("HO", minFragmentPartition.getObjectSet().iterator().next());



		SimpleContextEntry<String> affectedRu = (SimpleContextEntry<String>) ruStuckWaiting.getContextHandler().getContext().getContextEntryMap().get("AFFECTED-RU");
		/* Assert affected RUs */
		assertEquals(1, affectedRu.getObjectSet().size());
		assertEquals("ru-c2e15-13-01.cms",
				affectedRu.getObjectSet().iterator().next());

		SimpleContextEntry<String> affectedFed = (SimpleContextEntry<String>) ruStuckWaiting.getContextHandler().getContext().getContextEntryMap().get("AFFECTED-FED");
		/* Assert affected FEDs */
		assertEquals(1, affectedFed.getObjectSet().size());
		assertEquals(735, affectedFed.getObjectSet().iterator().next());
	}

}
