package rcms.utilities.daqexpert.reasoning.logic.failures.backpressure;

import org.junit.Test;
import rcms.utilities.daqaggregator.data.*;
import rcms.utilities.daqexpert.processing.context.ObjectContextEntry;
import rcms.utilities.daqexpert.reasoning.logic.failures.FlowchartCaseTestBase;

import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

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
		ObjectContextEntry<FED> problemFeds = (ObjectContextEntry<FED>) ruStuckWaiting.getContextHandler().getContext().getContextEntryMap().get("PROBLEM-FED");
		ObjectContextEntry<TTCPartition> problemPartitions = (ObjectContextEntry<TTCPartition>) ruStuckWaiting.getContextHandler().getContext().getContextEntryMap().get("PROBLEM-TTCP");
		ObjectContextEntry<SubSystem> problemSubsystems = (ObjectContextEntry<SubSystem>) ruStuckWaiting.getContextHandler().getContext().getContextEntryMap().get("PROBLEM-SUBSYSTEM");
		ObjectContextEntry<FEDBuilder> problemFedBuilder = (ObjectContextEntry<FEDBuilder>) ruStuckWaiting.getContextHandler().getContext().getContextEntryMap().get("PROBLEM-FED-BUILDER");

		assertEquals(8, problemFeds.getObjectSet().size());
		List<Integer> r = problemFeds.getObjectSet().stream().map(f -> ((FED) f).getSrcIdExpected()).collect(Collectors.toList());
		assertThat(r, hasItem(724));
		assertThat(r, hasItem(725));
		assertThat(r, hasItem(726));
		assertThat(r, hasItem(727));
		assertThat(r, hasItem(728));
		assertThat(r, hasItem(729));
		assertThat(r, hasItem(730));
		assertThat(r, hasItem(731));

		/* Assert problem partition and subsystem */
		assertEquals(1, problemPartitions.getObjectSet().size());
		assertEquals("HO", problemPartitions.getObjectSet().iterator().next().getName());
		assertEquals(1, problemSubsystems.getObjectSet().size());
		assertEquals("HCAL", problemSubsystems.getObjectSet().iterator().next().getName());

		/* Assert problem fedBuilder */
		assertEquals(1, problemFedBuilder.getObjectSet().size());
		assertEquals("HOSCAL", problemFedBuilder.getObjectSet().iterator().next().getName());


		ObjectContextEntry<Long> minFragmentCount = (ObjectContextEntry<Long>) ruStuckWaiting.getContextHandler().getContext().getContextEntryMap().get("MIN-FRAGMENT-COUNT");
		ObjectContextEntry<Long> maxFragmentCount = (ObjectContextEntry<Long>) ruStuckWaiting.getContextHandler().getContext().getContextEntryMap().get("MAX-FRAGMENT-COUNT");
		ObjectContextEntry<Long> minFragmentPartition = (ObjectContextEntry<Long>) ruStuckWaiting.getContextHandler().getContext().getContextEntryMap().get("MIN-FRAGMENT-PARTITION");
		ObjectContextEntry<Long> maxFragmentPartition = (ObjectContextEntry<Long>) ruStuckWaiting.getContextHandler().getContext().getContextEntryMap().get("MAX-FRAGMENT-PARTITION");
		/* Assert trigger count info */
		assertEquals(1, minFragmentCount.getObjectSet().size());
		assertEquals(1, maxFragmentCount.getObjectSet().size());
		assertEquals(1, minFragmentPartition.getObjectSet().size());
		assertEquals(1, maxFragmentPartition.getObjectSet().size());
		assertEquals(4731L, maxFragmentCount.getObjectSet().iterator().next().longValue());
		assertEquals(0L, minFragmentCount.getObjectSet().iterator().next().longValue());
		assertEquals("SCAL", maxFragmentPartition.getObjectSet().iterator().next());
		assertEquals("HO", minFragmentPartition.getObjectSet().iterator().next());



		ObjectContextEntry<String> affectedRu = (ObjectContextEntry<String>) ruStuckWaiting.getContextHandler().getContext().getContextEntryMap().get("AFFECTED-RU");
		/* Assert affected RUs */
		assertEquals(1, affectedRu.getObjectSet().size());
		assertEquals("ru-c2e15-13-01.cms",
				affectedRu.getObjectSet().iterator().next());

		ObjectContextEntry<FED> affectedFed = (ObjectContextEntry<FED>) ruStuckWaiting.getContextHandler().getContext().getContextEntryMap().get("AFFECTED-FED");
		/* Assert affected FEDs */
		assertEquals(1, affectedFed.getObjectSet().size());
		assertEquals(735, affectedFed.getObjectSet().iterator().next().getSrcIdExpected());
	}

}
