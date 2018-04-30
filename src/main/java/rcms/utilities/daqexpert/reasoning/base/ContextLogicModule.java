package rcms.utilities.daqexpert.reasoning.base;

import rcms.utilities.daqexpert.processing.Requiring;
import rcms.utilities.daqexpert.processing.context.ContextHandler;

public abstract class ContextLogicModule extends SimpleLogicModule {

	/**
	 * ContextHandler is used to parameterize action and description fields with
	 * specific contextHandler information. Variables will be replaced with values from
	 * this contextHandler
	 */
	protected final ContextHandler contextHandler;


	public ContextLogicModule() {
		this.contextHandler = new ContextHandler();
	}

	public ContextHandler getContextHandler() {
		return contextHandler;
	}

	public String getDescriptionWithContext() {
		String result = this.getContextHandler().putContext(this.description);

		for(Requiring requiring: required){
			if(requiring instanceof  ContextLogicModule){
				result  = ((ContextLogicModule) requiring).getContextHandler().putContext(result);
			}
		}

		return result;
	}

}
