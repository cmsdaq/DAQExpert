package rcms.utilities.daqexpert.reasoning.logic.failures;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import rcms.utilities.daqaggregator.data.DAQ;

public class RuStuckWaitingTest extends FlowchartCaseTestBase {

	@Test
	public void test() throws URISyntaxException {
		Logger.getLogger(BackpressureAnalyzer.class).setLevel(Level.TRACE);
		DAQ snapshot = getSnapshot("1491069352157.smile");
		Map<String, Boolean> results = new HashMap();
		results.put("StableBeams", false);
		results.put("NoRateWhenExpected", true);

		assertEquals(false, fc1.satisfied(snapshot, results));
		assertEquals(false, fc2.satisfied(snapshot, results));
		assertEquals(false, fc3.satisfied(snapshot, results));
		assertEquals(false, fc4.satisfied(snapshot, results));
		assertEquals(false, fc5.satisfied(snapshot, results));
		assertEquals(true, ruStuckWaiting.satisfied(snapshot, results));

		System.out.println("New message:");
		System.out.println(ruStuckWaiting.getDescriptionWithContext());

		/* Assert problem FEDs */
		assertEquals(8, ruStuckWaiting.getContext().getContext().get("PROBLEM-FED").size());
		assertThat(ruStuckWaiting.getContext().getContext().get("PROBLEM-FED"), hasItem(724));
		assertThat(ruStuckWaiting.getContext().getContext().get("PROBLEM-FED"), hasItem(725));
		assertThat(ruStuckWaiting.getContext().getContext().get("PROBLEM-FED"), hasItem(726));
		assertThat(ruStuckWaiting.getContext().getContext().get("PROBLEM-FED"), hasItem(727));
		assertThat(ruStuckWaiting.getContext().getContext().get("PROBLEM-FED"), hasItem(728));
		assertThat(ruStuckWaiting.getContext().getContext().get("PROBLEM-FED"), hasItem(729));
		assertThat(ruStuckWaiting.getContext().getContext().get("PROBLEM-FED"), hasItem(730));
		assertThat(ruStuckWaiting.getContext().getContext().get("PROBLEM-FED"), hasItem(731));

		/* Assert problem partition and subsystem */
		assertEquals(1, ruStuckWaiting.getContext().getContext().get("PROBLEM-TTCP").size());
		assertEquals("HO", ruStuckWaiting.getContext().getContext().get("PROBLEM-TTCP").iterator().next());
		assertEquals(1, ruStuckWaiting.getContext().getContext().get("PROBLEM-SUBSYSTEM").size());
		assertEquals("HCAL", ruStuckWaiting.getContext().getContext().get("PROBLEM-SUBSYSTEM").iterator().next());

		/* Assert problem fedBuilder */
		assertEquals(1, ruStuckWaiting.getContext().getContext().get("PROBLEM-FED-BUILDER").size());
		assertEquals("HOSCAL", ruStuckWaiting.getContext().getContext().get("PROBLEM-FED-BUILDER").iterator().next());

		/* Assert trigger count info */
		assertEquals(1, ruStuckWaiting.getContext().getContext().get("MIN-FRAGMENT-COUNT").size());
		assertEquals(1, ruStuckWaiting.getContext().getContext().get("MAX-FRAGMENT-COUNT").size());
		assertEquals(1, ruStuckWaiting.getContext().getContext().get("MIN-FRAGMENT-PARTITION").size());
		assertEquals(1, ruStuckWaiting.getContext().getContext().get("MAX-FRAGMENT-PARTITION").size());
		assertEquals(4731L, ruStuckWaiting.getContext().getContext().get("MAX-FRAGMENT-COUNT").iterator().next());
		assertEquals(0L, ruStuckWaiting.getContext().getContext().get("MIN-FRAGMENT-COUNT").iterator().next());
		assertEquals("SCAL", ruStuckWaiting.getContext().getContext().get("MAX-FRAGMENT-PARTITION").iterator().next());
		assertEquals("HO", ruStuckWaiting.getContext().getContext().get("MIN-FRAGMENT-PARTITION").iterator().next());

		/* Assert affected RUs */
		assertEquals(1, ruStuckWaiting.getContext().getContext().get("AFFECTED-RU").size());
		assertEquals("ru-c2e15-13-01.cms",
				ruStuckWaiting.getContext().getContext().get("AFFECTED-RU").iterator().next());

		/* Assert affected FEDs */
		assertEquals(1, ruStuckWaiting.getContext().getContext().get("AFFECTED-FED").size());
		assertEquals(735, ruStuckWaiting.getContext().getContext().get("AFFECTED-FED").iterator().next());
	}

}
