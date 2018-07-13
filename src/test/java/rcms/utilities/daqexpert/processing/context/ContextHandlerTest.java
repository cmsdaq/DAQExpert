package rcms.utilities.daqexpert.processing.context;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class ContextHandlerTest {


    @Test
    public void retrievalOfMultikeyTest(){
        ContextHandler ch = new ContextHandler();
        String text = "abc {{CONTRIBUTIONS_*}} def {{OTHER_*}} def  def";
        assertThat(ch.getMultiKeys(text), Matchers.contains("CONTRIBUTIONS_","OTHER_"));

        text.replaceAll("\\{\\{CONTRIBUTIONS\\_\\*\\}\\}", "A");

    }

}