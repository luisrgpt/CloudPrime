package pt.ulisboa.tecnico.cnv.cloudprime;


import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import BIT.highBIT.BasicBlock;
import BIT.highBIT.ClassInfo;
import BIT.highBIT.Instruction;
import BIT.highBIT.InstructionTable;
import BIT.highBIT.Routine;

public class BytecodeAnalyser {

	private static int _instructionTypeCounter0 = 0,
			           _instructionTypeCounter1 = 0,
					   _instructionTypeCounter2 = 0,
					   _instructionTypeCounter3 = 0,
					   _instructionTypeCounter4 = 0,
					   _instructionTypeCounter5 = 0;
	
	private static final String CLASSNAME = BytecodeAnalyser.class.getName().replace(".", "/");

	public static synchronized void countInstruction2(int increment) {
		_instructionTypeCounter0 += increment;
	}

	public static synchronized void countInstruction4(int increment) {
		_instructionTypeCounter1 += increment;
	}

	public static synchronized void countInstruction5(int increment) {
		_instructionTypeCounter2 += increment;
	}

	public static synchronized void countInstruction9(int increment) {
		_instructionTypeCounter3 += increment;
	}

	public static synchronized void countInstruction10(int increment) {
		_instructionTypeCounter4 += increment;
	}

	public static synchronized void countInstructionOther(int increment) {
		_instructionTypeCounter5 += increment;
	}

	public static synchronized void printMetrics(int foo) {
		System.err.println("Instruction Types:" + System.lineSeparator() + 
				" MEMORY_INSTRUCTION:        " + _instructionTypeCounter0 + System.lineSeparator() +	// LOAD_INSTRUCTION,
																										// STORE_INSTRUCTION
				
		        " STACK_INSTRUCTION:         " + _instructionTypeCounter1 + System.lineSeparator() +	// STACK_INSTRUCTION
		        
		        " ALU_INSTRUCTION:           " + _instructionTypeCounter2 + System.lineSeparator() +	// ARITHMETIC_INSTRUCTION,
																										// LOGICAL_INSTRUCTION,
																										// CONVERSION_INSTRUCTION,
																										// COMPARISON_INSTRUCTION
		        
				" CONDITIONAL_INSTRUCTION:   " + _instructionTypeCounter3 + System.lineSeparator() +	// CONDITIONAL_INSTRUCTION
				
				" UNCONDITIONAL_INSTRUCTION: " + _instructionTypeCounter4 + System.lineSeparator() +	// UNCONDITIONAL_INSTRUCTION
				
				" OTHER:                     " + _instructionTypeCounter5 + System.lineSeparator());	// NOP_INSTRUCTION,
																										// CONSTANT_INSTRUCTION,
																										// CLASS_INSTRUCTION,
																										// OBJECT_INSTRUCTION,
																										// EXCEPTION_INSTRUCTION,
																										// INSTRUCTIONCHECK_INSTRUCTION,
																										// MONITOR_INSTRUCTION,
																										// OTHER_INSTRCTION
	}

	@SuppressWarnings("rawtypes")
	public static <T extends Object> void instrumentalizeClass(Class<T> genericClass)
			throws IOException, InterruptedException {
		String classname = genericClass.getName();
		System.out.print("Instrumentalizing class " + classname + "..." );
	    
		String filename = "target/classes/" + classname.replace(".", "/") + ".class";
		if (!new File(filename).isFile()) {
			System.out.println(" error! Cannot find class " + filename + ".");
			return;
		}

		ClassInfo classInfo = new ClassInfo(filename);

		int numberOfMetrics = 16, otherAcc = 0, twoAcc = 0, fiveAcc = 0, index;
		int[] localMetrics = new int[numberOfMetrics];
		for (Enumeration enumeration = classInfo.getRoutines().elements(); enumeration.hasMoreElements();) {
			Routine routine = (Routine) enumeration.nextElement();

			Instruction[] instructions = routine.getInstructions();
			for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements();) {
				BasicBlock basicBlock = (BasicBlock) b.nextElement();
				List<Instruction> instructionList = Arrays.asList(instructions).subList(basicBlock.getStartAddress(),
						basicBlock.getEndAddress() + 1);

				for (Instruction instruction : instructionList) {
					short opcodeType = InstructionTable.InstructionTypeTable[instruction.getOpcode()];
					++localMetrics[opcodeType];
				}

				for (index = 0; index < 2; ++index) {
					otherAcc += localMetrics[index];
					localMetrics[index] = 0;
				}

				for (index = 2; index < 4; ++index) {
					twoAcc += localMetrics[index];
					localMetrics[index] = 0;
				}

				if (localMetrics[4] > 0) {
					basicBlock.addBefore(CLASSNAME, "countInstruction4", new Integer(localMetrics[index]));
					localMetrics[4] = 0;
				}

				for (index = 5; index < 9; ++index) {
					fiveAcc += localMetrics[index];
					localMetrics[index] = 0;
				}

				for (index = 9; index < 11; ++index) {
					if (localMetrics[index] > 0) {
						basicBlock.addBefore(CLASSNAME, "countInstruction" + index, new Integer(localMetrics[index]));
						localMetrics[index] = 0;
					}
				}

				for (index = 11; index < 16; ++index) {
					otherAcc += localMetrics[index];
					localMetrics[index] = 0;
				}

				if (otherAcc > 0) {
					basicBlock.addBefore(CLASSNAME, "countInstructionOther", new Integer(otherAcc));
					otherAcc = 0;
				}

				if (twoAcc > 0) {
					basicBlock.addBefore(CLASSNAME, "countInstruction2", new Integer(twoAcc));
					twoAcc = 0;
				}

				if (fiveAcc > 0) {
					basicBlock.addBefore(CLASSNAME, "countInstruction5", new Integer(fiveAcc));
					fiveAcc = 0;
				}
			}
		}

		classInfo.addAfter(CLASSNAME, "printMetrics", new Integer(0));
		classInfo.write(filename);
		System.out.println(" Done!");
	}
}