package rcms.utilities.daqexpert.processing.context.functions;

import rcms.utilities.daqaggregator.data.FED;

import java.util.function.Function;

public class FedPrinter implements Function<FED, String> {

    @Override
    public String apply(FED fed) {
        return Integer.toString(fed.getSrcIdExpected());
    }
}
