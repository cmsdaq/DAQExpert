package rcms.utilities.daqexpert;

import java.util.Properties;

public class FailFastParameterReader {

    public static String getStringParameter(Properties properties, String key, Class logicModuleClass) {

        try {
            String result = properties.getProperty(key);

            if(result == null){
                throw new ExpertException(ExpertExceptionCode.MissingProperty, "Missing property " + key + ", it's required by LM " + logicModuleClass.getSimpleName());
            }
            return result;

        } catch (NullPointerException e) {
            throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException,
                    "Could not parametrize LM " + logicModuleClass.getSimpleName() + ", properties not provided");
        }

    }

    public static Float getFloatParameter(Properties properties, Setting setting, Class logicModuleClass) {
        return getFloatParameter(properties, setting.getKey(), logicModuleClass);
    }

    public static Float getFloatParameter(Properties properties, String key, Class logicModuleClass) {
        String value = null;
        try {
            value = getStringParameter(properties, key, logicModuleClass);
            return Float.parseFloat(value);

        } catch (NumberFormatException e) {
            throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException, "Could not update LM "
                    + logicModuleClass.getSimpleName() + ", failed to parse " + value + " as float from property " + key + " number parsing problem: " + e.getMessage());
        }
    }


    public static Integer getIntegerParameter(Properties properties, Setting setting, Class logicModuleClass) {
        return getIntegerParameter(properties, setting.getKey(), logicModuleClass);
    }

    public static Integer getIntegerParameter(Properties properties, String key, Class logicModuleClass) {
        String value = null;
        try {
            value = getStringParameter(properties, key, logicModuleClass);
            return Integer.parseInt(value);

        } catch (NumberFormatException e) {
            throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException, "Could not update LM "
                    + logicModuleClass.getSimpleName() + ", failed to parse " + value + " as int from property " + key + " number parsing problem: " + e.getMessage());
        }
    }

    public static Boolean getBooleanParameter(Properties properties, Setting setting, Class logicModuleClass) {
        return getBooleanParameter(properties, setting.getKey(), logicModuleClass);
    }

    public static Boolean getBooleanParameter(Properties properties, String key, Class logicModuleClass) {
        String value = null;
        try {
            value = getStringParameter(properties, key, logicModuleClass);
            return Boolean.parseBoolean(value);

        } catch (NumberFormatException e) {
            throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException, "Could not update LM "
                    + logicModuleClass.getSimpleName() + ", failed to parse " + value + " as boolean from property " + key + " number parsing problem: " + e.getMessage());
        }
    }
}
