package rcms.utilities.daqexpert.reasoning.base;

import org.junit.Assert;
import org.junit.Test;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;

import java.util.stream.Collectors;

public class ResultSupplierTest {

    @Test
    public void test(){

        ResultSupplier rs = new ResultSupplier();

        rs.update(LogicModuleRegistry.NoRate, new Output(true));


        LogicModule lm1 = generateAnonymousLm("No rate when expected", LogicModuleRegistry.NoRateWhenExpected, LogicModuleRegistry.NoRate);
        LogicModule lm2 = generateAnonymousLm("No rate", LogicModuleRegistry.NoRate, null);

        lm1.declareRelations();
        lm2.declareRelations();

        Assert.assertNotNull(lm1.getLogicModuleRegistry());
        Assert.assertNotNull(lm2.getLogicModuleRegistry());

        System.out.println(lm1.getNodeName() + " requires: " + lm1.getRequired().stream().map(l->l.getNodeName()).collect(Collectors.toList()));
        System.out.println(lm2.getNodeName() + " requires: " + lm2.getRequired().stream().map(l->l.getNodeName()).collect(Collectors.toList()));

        Assert.assertNotNull( rs.get(LogicModuleRegistry.NoRateWhenExpected, LogicModuleRegistry.NoRate));


        try {
            Assert.assertNotNull(rs.get(LogicModuleRegistry.NoRate, LogicModuleRegistry.NoRateWhenExpected));
            Assert.fail("Exception was not thrown");
        } catch(ExpertException e){
            Assert.assertNotNull(e);
        }
    }


    private LogicModule generateAnonymousLm(String anonyousName, LogicModuleRegistry registry, final LogicModuleRegistry requiredLm){

        LogicModule anonymousLm = new LogicModule() {


            @Override
            public LogicModuleRegistry getLogicModuleRegistry() {
                return registry;
            }

            @Override
            public void declareRelations() {

                if(requiredLm != null) {
                    require(requiredLm);
                }
            }

            @Override
            public String getName() {
                return anonyousName;
            }
        };
        registry.setLogicModule(anonymousLm);

        return anonymousLm;

    }

}
