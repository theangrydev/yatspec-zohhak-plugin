package io.github.theangrydev.yatspeczohhakplugin;

final class Result {

	static final Result FAILURE = new Result();

	private Object result;
	private final boolean succeeded;

	static Result success(Object result) {
		return new Result(result);
	}
	
	private Result(Object result) {
		this.result = result;
		succeeded = true;
	}

	private Result() {
		this.succeeded = false;
	}
	
	boolean succeeded() {
		return succeeded;
	}
	
	Object getResult() {
		return result;
	}
}
