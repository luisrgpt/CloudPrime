package pt.ulisboa.tecnico.cnv.cloudprime;

import BIT.highBIT.ClassInfo;
import BIT.highBIT.Routine;

public class MultiProcessedBytecodeAnalyser extends BytecodeAnalyser {
	private static int 	_instructionTypeCounter0 = 0,
						//_instructionTypeCounter1 = 0,
						//_instructionTypeCounter2 = 0,
						_instructionTypeCounter3 = 0,
						_instructionTypeCounter4 = 0,
						_instructionTypeCounter5 = 0;
	
	@Override
	protected String getClassname() {
		return MultiProcessedBytecodeAnalyser.class.getName().replace(".", "/");
	}
	
	@Override
	protected void initMultiThreadedTable(Routine routine) {}

	public static synchronized void countInstruction2(int increment) {
		_instructionTypeCounter0 += increment;
	}

	//public static synchronized void countInstruction4(int increment) {
	//	_instructionTypeCounter1 += increment;
	//}

	//public static synchronized void countInstruction5(int increment) {
	//	_instructionTypeCounter2 += increment;
	//}

	public static synchronized void countInstruction9(int increment) {
		_instructionTypeCounter3 += increment;
	}

	public static synchronized void countInstruction10(int increment) {
		_instructionTypeCounter4 += increment;
	}

	public static synchronized void countInstructionOther(int increment) {
		_instructionTypeCounter5 += increment;
	}
	
	@Override
	protected void initPrintMetrics(ClassInfo classInfo) {
		classInfo.addAfter(getClassname(), "printMetrics", new Integer(0));
	}

	public static synchronized void printMetrics(int foo) {
		System.err.println("[Multi-Process] Instruction Types:" + System.lineSeparator() + 
				" MEMORY_INSTRUCTION:        " + _instructionTypeCounter0 + System.lineSeparator() +	// LOAD_INSTRUCTION,
																										// STORE_INSTRUCTION

				" CONDITIONAL_INSTRUCTION:   " + _instructionTypeCounter3 + System.lineSeparator() +	// CONDITIONAL_INSTRUCTION

				" UNCONDITIONAL_INSTRUCTION: " + _instructionTypeCounter4 + System.lineSeparator() +	// UNCONDITIONAL_INSTRUCTION

				" OTHER:                     " + _instructionTypeCounter5 + System.lineSeparator());	// NOP_INSTRUCTION,
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
