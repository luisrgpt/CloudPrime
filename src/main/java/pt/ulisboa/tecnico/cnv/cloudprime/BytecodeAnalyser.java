package pt.ulisboa.tecnico.cnv.cloudprime;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import BIT.highBIT.BasicBlock;
import BIT.highBIT.ClassInfo;
import BIT.highBIT.Instruction;
import BIT.highBIT.InstructionTable;
import BIT.highBIT.Routine;

public class BytecodeAnalyser {
	//private static Map<Long, Integer[]> _instructionCounter = new HashMap<>();
	private static int[] _instructionTypeCounter = new int [6];
	
	private static final String CLASSNAME = BytecodeAnalyser.class.getName().replace(".", "/");

	/*
	public static synchronized void addThreadMetrics(int increment) {
		// _instructionTypeCounter.put(Thread.currentThread().getId(), new
		// Integer[17]);
		Integer[] intergers = new Integer[16];
		for (int index = 0; index < 16; ++index) {
			intergers[index] = new Integer(0);
		}
		_instructionCounter.put(Thread.currentThread().getId(), intergers);
		System.out.println(Thread.currentThread().getId());
	}

	public static synchronized void countInstruction2(int increment) {
		Integer[] integers = _instructionCounter.get(Thread.currentThread().getId());
		integers[2] += increment;
		_instructionCounter.put(Thread.currentThread().getId(), integers);
	}

	public static synchronized void countInstruction4(int increment) {
		Integer[] integers = _instructionCounter.get(Thread.currentThread().getId());
		integers[4] += increment;
		_instructionCounter.put(Thread.currentThread().getId(), integers);
	}

	public static synchronized void countInstruction5(int increment) {
		Integer[] integers = _instructionCounter.get(Thread.currentThread().getId());
		integers[5] += increment;
		_instructionCounter.put(Thread.currentThread().getId(), integers);
	}

	public static synchronized void countInstruction9(int increment) {
		Integer[] integers = _instructionCounter.get(Thread.currentThread().getId());
		integers[9] += increment;
		_instructionCounter.put(Thread.currentThread().getId(), integers);
	}

	public static synchronized void countInstruction10(int increment) {
		Integer[] integers = _instructionCounter.get(Thread.currentThread().getId());
		integers[10] += increment;
		_instructionCounter.put(Thread.currentThread().getId(), integers);
	}

	public static synchronized void countInstructionOther(int increment) {
		Integer[] integers = _instructionCounter.get(Thread.currentThread().getId());
		integers[0] += increment;
		_instructionCounter.put(Thread.currentThread().getId(), integers);
	}
	
		public static synchronized String getMetricsString() {
		System.out.println(Thread.currentThread().getId());
		Integer[] integers = _instructionCounter.get(Thread.currentThread().getId());
		return "Exaustive summary:" + System.lineSeparator() +
				"Instruction Types:" + System.lineSeparator() + 
				" MEMORY_INSTRUCTION:           " + integers[2] + System.lineSeparator() + 		// LOAD_INSTRUCTION,
																								// STORE_INSTRUCTION
		        " STACK_INSTRUCTION:            " + integers[4] + System.lineSeparator() +
		        " ALU_INSTRUCTION:              " + integers[5] + System.lineSeparator() + 		// ARITHMETIC,
																								// LOGICAL,
																								// CONVERSION,
																								// COMPARISON
				" CONDITIONAL_INSTRUCTION:      " + integers[9] + System.lineSeparator() +
				" UNCONDITIONAL_INSTRUCTION:    " + integers[10] + System.lineSeparator() +
				" OTHER:                        " + integers[0] + System.lineSeparator(); 		// NOP_INSTRUCTION,
																								// CONSTANT_INSTRUCTION,
																								// CLASS_INSTRUCTION,
																								// OBJECT_INSTRUCTION,
																								// EXCEPTION_INSTRUCTION,
																								// INSTRUCTIONCHECK_INSTRUCTION,
																								// MONITOR_INSTRUCTION,
																								// OTHER_INSTRCTION
	}
	*/
	
	public static synchronized void countInstruction2(int increment) {
		_instructionTypeCounter[0] += increment;
	}

	public static synchronized void countInstruction4(int increment) {
		_instructionTypeCounter[1] += increment;
	}

	public static synchronized void countInstruction5(int increment) {
		_instructionTypeCounter[2] += increment;
	}

	public static synchronized void countInstruction9(int increment) {
		_instructionTypeCounter[3] += increment;
	}

	public static synchronized void countInstruction10(int increment) {
		_instructionTypeCounter[4] += increment;
	}

	public static synchronized void countInstructionOther(int increment) {
		_instructionTypeCounter[5] += increment;
	}

	public static synchronized void printMetrics(int foo) {
		System.err.println("Exaustive summary:" + System.lineSeparator() +
				"Instruction Types:" + System.lineSeparator() + 
				" MEMORY_INSTRUCTION:           " + _instructionTypeCounter[0] + System.lineSeparator() + 		// LOAD_INSTRUCTION,
																								// STORE_INSTRUCTION
		        " STACK_INSTRUCTION:            " + _instructionTypeCounter[1] + System.lineSeparator() +
		        " ALU_INSTRUCTION:              " + _instructionTypeCounter[2] + System.lineSeparator() + 		// ARITHMETIC,
																								// LOGICAL,
																								// CONVERSION,
																								// COMPARISON
				" CONDITIONAL_INSTRUCTION:      " + _instructionTypeCounter[3] + System.lineSeparator() +
				" UNCONDITIONAL_INSTRUCTION:    " + _instructionTypeCounter[4] + System.lineSeparator() +
				" OTHER:                        " + _instructionTypeCounter[5] + System.lineSeparator()); 		// NOP_INSTRUCTION,
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