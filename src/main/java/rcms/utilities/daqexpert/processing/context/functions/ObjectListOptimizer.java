package rcms.utilities.daqexpert.processing.context.functions;

import rcms.utilities.daqaggregator.data.FED;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ObjectListOptimizer<T> implements Serializable {

    public String getShortestListRepresentation(Set<T> objects, Function<T, Object> function){

        boolean allInteger = true;
        for(T object: objects){
            Object r= function.apply(object);
            if(r instanceof Integer){
                // nothing to do here, check other elements
            } else if(r instanceof String){
                try{
                    Integer.parseInt((String) r);
                } catch(NumberFormatException e){
                    allInteger = false;
                    break; // no need to check other elements anymore
                }
            } else{
                allInteger = false;
                break; // no need to check other elements anymore
            }
        }

        if(allInteger){
            return getShortestIntegerListRepresentation(objects, function.andThen(c->c instanceof  Integer? (Integer) c : Integer.parseInt((String)c)));
        } else{
            int limit = 4;
            String representation = objects.stream().map(o->function.apply(o).toString()).sorted().limit(limit).collect(Collectors.joining(", "));

            if(objects.size() > limit){
                representation += " and " + (objects.size() - limit ) + " more";
            }

            if(objects.size() > 1){
                representation = "[" + representation + "]";
            }
            return representation;
        }
    }

    /**
     *
     * @param objects set of any objects that will be represented
     * @param function function to get the object identifier
     * @return
     */
    public String getShortestIntegerListRepresentation(Set<T> objects, Function<T, Integer> function){

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
