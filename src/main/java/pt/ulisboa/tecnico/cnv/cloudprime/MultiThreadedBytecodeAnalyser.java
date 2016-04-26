package pt.ulisboa.tecnico.cnv.cloudprime;

import java.util.HashMap;
import java.util.Map;

import BIT.highBIT.ClassInfo;
import BIT.highBIT.Routine;

final class MultiThreadedBytecodeAnalyser extends BytecodeAnalyser {
	private static Map<Long, Integer[]> _threadedInstructionCounter = new HashMap<>();
	//private static long[][] _alternativeThreadedInstructionCounter = new long[(int) Long.MAX_VALUE][];
	
	
	@Override
	protected String getClassname() {
		return MultiThreadedBytecodeAnalyser.class.getName().replace(".", "/");
	}
	
	@Override
	protected void initMultiThreadedTable(Routine routine) {
		if (routine.getMethodName().equals("getResponse")) {
			routine.addBefore(getClassname(), "addThreadMetrics", new Integer(0));
		}
	}
	
	public static synchronized void addThreadMetrics(int increment) {
		Integer[] intergers = new Integer[6];
		for (int index = 0; index < 6; ++index) {
			intergers[index] = new Integer(0);
		}
		_threadedInstructionCounter.put(Thread.currentThread().getId(), intergers);
	}
	
	public static synchronized void countInstruction2(int increment) {
		Integer[] integers = _threadedInstructionCounter.get(Thread.currentThread().getId());
		integers[0] += increment;
		_threadedInstructionCounter.put(Thread.currentThread().getId(), integers);
	}

	public static synchronized void countInstruction4(int increment) {
		Integer[] integers = _threadedInstructionCounter.get(Thread.currentThread().getId());
		integers[1] += increment;
		_threadedInstructionCounter.put(Thread.currentThread().getId(), integers);
	}

	public static synchronized void countInstruction5(int increment) {
		Integer[] integers = _threadedInstructionCounter.get(Thread.currentThread().getId());
		integers[2] += increment;
		_threadedInstructionCounter.put(Thread.currentThread().getId(), integers);
	}

	public static synchronized void countInstruction9(int increment) {
		Integer[] integers = _threadedInstructionCounter.get(Thread.currentThread().getId());
		integers[3] += increment;
		_threadedInstructionCounter.put(Thread.currentThread().getId(), integers);
	}

	public static synchronized void countInstruction10(int increment) {
		Integer[] integers = _threadedInstructionCounter.get(Thread.currentThread().getId());
		integers[4] += increment;
		_threadedInstructionCounter.put(Thread.currentThread().getId(), integers);
	}

	public static synchronized void countInstructionOther(int increment) {
		Integer[] integers = _threadedInstructionCounter.get(Thread.currentThread().getId());
		integers[5] += increment;
		_threadedInstructionCounter.put(Thread.currentThread().getId(), integers);
	}
	
	@Override
	protected void initPrintMetrics(ClassInfo classInfo) {}

	public static String getMetricsString() {
		Integer[] integers = _threadedInstructionCounter.get(Thread.currentThread().getId());
		return "[Multi-Thread] Instruction Types:" + System.lineSeparator() + 
				" MEMORY_INSTRUCTION:        " + integers[0] + System.lineSeparator() +	// LOAD_INSTRUCTION,
																						// STORE_INSTRUCTION
				
				" CONDITIONAL_INSTRUCTION:   " + integers[3] + System.lineSeparator() +	// CONDITIONAL_INSTRUCTION
				
				" UNCONDITIONAL_INSTRUCTION: " + integers[4] + System.lineSeparator() +	// UNCONDITIONAL_INSTRUCTION
				
				" OTHER:                     " + integers[5] + System.lineSeparator();	// NOP_INSTRUCTION,
																						// CONSTANT_INSTRUCTION,
																						// STACK_INSTRUCTION
																						// ARITHMETIC_INSTRUCTION,
																						// LOGICAL_INSTRUCTION,
																						// CONVERSION_INSTRUCTION,
																						// COMPARISON_INSTRUCTION
																						// CLASS_INSTRUCTION,
																						// OBJECT_INSTRUCTION,
																						// EXCEPTION_INSTRUCTION,
																						// INSTRUCTIONCHECK_INSTRUCTION,
																						// MONITOR_INSTRUCTION,
																						// OTHER_INSTRCTION
	}
}
