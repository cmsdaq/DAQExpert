package rcms.utilities.daqexpert.processing.context.functions;

import rcms.utilities.daqaggregator.data.FED;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ObjectListOptimizer<T> {

    /**
     *
     * @param objects set of any objects that will be represented
     * @param function function to get the object identifier
     * @return
     */
    public String getShortestListRepresentation(Set<T> objects, Function<T, Integer> function){

        if(objects.size() == 0){
            return "";
        } else if(objects.size() == 1 ){
            return function.apply(objects.iterator().next()) + "";
        } else{
            String rangeRepresentation = getRangeRepresentation(objects, function);

            String simpleRepresentation = getSimpleRepresentation(objects, function);

            if(rangeRepresentation.length() < simpleRepresentation.length()){


                return rangeRepresentation;
            } else{
                return simpleRepresentation;
            }

        }


    }

    protected String getSimpleRepresentation(Set<T> objects, Function<T, Integer> function){
        List<Integer> representation = objects.stream().map(o->function.apply(o)).collect(Collectors.toList());

        return representation.size() == 1 ? representation.iterator().next().toString() : representation.toString();

    }


    protected String getRangeRepresentation(Set<T> objects, Function<T, Integer> function){

        List<T> sorted = objects.stream().sorted((o1, o2) -> function.apply(o1) > function.apply(o2) ? 1: -1).collect(Collectors.toList());

        List<String> ranges = convertSortedToRanges(sorted, function);

        return ranges.size() == 1 ? ranges.iterator().next(): ranges.toString();
    }

    protected List<String> convertSortedToRanges(List<T> objects, Function<T, Integer> function){
        List<String> ranges = new ArrayList<>();
        Integer last = null;
        for(T o: objects){

            int current = function.apply(o);

            if(last == null){
                ranges.add(current + "");
            } else{

                int lastElementIndex = ranges.size() - 1;
                String lastValue = ranges.get(lastElementIndex);

                if(current  == last + 1){
                    // ommit that as there is sequence

                    if(!lastValue.endsWith("-")){
                        ranges.set(lastElementIndex, lastValue + "-");
                    }
                } else{

                    if(lastValue.endsWith("-")){
                        ranges.set(lastElementIndex, lastValue + last);
                    }
                    ranges.add(current + "");
                }

            }

            last = function.apply(o);

        }

        if(ranges.size() > 0 && ranges.get(ranges.size() -1).endsWith("-")){
            ranges.set(ranges.size()-1, ranges.get(ranges.size() -1) + last);
        }

        return ranges;
    }

    private String getAllButRepresentation(Set<T> objects, Function<T, Integer> function){

        return null;
    }


}
