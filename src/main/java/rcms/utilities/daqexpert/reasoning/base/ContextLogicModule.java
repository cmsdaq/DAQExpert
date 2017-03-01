package rcms.utilities.daqexpert.reasoning.base;

public abstract class ContextLogicModule extends SimpleLogicModule {

	/**
	 * Context is used to parameterize action and description fields with
	 * specific context information. Variables will be replaced with values from
	 * this context
	 */
	protected final Context context;

	public ContextLogicModule() {
		this.context = new Context();
	}

	public Context getContext() {
		return context;
	}

}
