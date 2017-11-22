package rcms.utilities.daqexpert;

import java.util.Properties;

public class FailFastParameterReader {

    public static String getStringParameter(Properties properties, Setting setting, Class logicModuleClass) {

        try {
            String result = properties.getProperty(setting.getKey());

            if(result == null){
                throw new ExpertException(ExpertExceptionCode.MissingProperty, "Missing property " + setting.getKey() + ", it's required by LM " + logicModuleClass.getSimpleName());
            }
            return result;

        } catch (NullPointerException e) {
            throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException,
                    "Could not update LM " + logicModuleClass.getSimpleName() + ", other problem: " + e.getMessage());
        }

    }

    public static Float getFloatParameter(Properties properties, Setting setting, Class logicModuleClass) {
        String value = null;
        try {
            value = getStringParameter(properties, setting, logicModuleClass);
            return Float.parseFloat(value);

        } catch (NumberFormatException e) {
            throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException, "Could not update LM "
                    + logicModuleClass.getSimpleName() + ", failed to parse " + value + " as float from property " + setting.getKey() + " number parsing problem: " + e.getMessage());
        }
    }

    public static Integer getIntegerParameter(Properties properties, Setting setting, Class logicModuleClass) {
        String value = null;
        try {
            value = getStringParameter(properties, setting, logicModuleClass);
            return Integer.parseInt(value);

        } catch (NumberFormatException e) {
            throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException, "Could not update LM "
                    + logicModuleClass.getSimpleName() + ", failed to parse " + value + " as int from property " + setting.getKey() + " number parsing problem: " + e.getMessage());
        }
    }

    public static Boolean getBooleanParameter(Properties properties, Setting setting, Class logicModuleClass) {
        String value = null;
        try {
            value = getStringParameter(properties, setting, logicModuleClass);
            return Boolean.parseBoolean(value);

        } catch (NumberFormatException e) {
            throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException, "Could not update LM "
                    + logicModuleClass.getSimpleName() + ", failed to parse " + value + " as boolean from property " + setting.getKey() + " number parsing problem: " + e.getMessage());
        }
    }
}
