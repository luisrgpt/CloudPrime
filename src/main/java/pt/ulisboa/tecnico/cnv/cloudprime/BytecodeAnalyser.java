package pt.ulisboa.tecnico.cnv.cloudprime;

import java.io.File;
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
	private static Map<Long, Integer[]> _instructionCounter = new HashMap<>();

	private static final String CLASSNAME = "BytecodeAnalyser";

	public static void printUsage() {
		System.out.println("Syntax: java BytecodeAnalyser -stat_type in_path [out_path]");
		System.out.println("        where stat_type can be:");
		System.out.println("        exaustive:  instruction counter");
		System.out.println();
		System.out.println("        in_path:  directory from which the class files are read");
		System.out.println("        out_path: directory to which the class files are written");
		System.out.println("        Both in_path and out_path are required unless stat_type is static");
		System.out.println("        in which case only in_path is required");
		System.exit(-1);
	}

	@SuppressWarnings("rawtypes")
	public static void doMetrics(File in_dir, File out_dir) {
		String infilenames[] = in_dir.list();
		int numberOfMetrics = 16;
		int[] localMetrics = new int[numberOfMetrics];

		for (String filename : infilenames) {
			if (filename.endsWith(".class")) {
				String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
				String out_filename = out_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
				ClassInfo ci = new ClassInfo(in_filename);

				for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements();) {
					Routine routine = (Routine) e.nextElement();

					if (routine.getMethodName().equals("getResponse")) {
						routine.addBefore(CLASSNAME, "addThreadMetrics", new Integer(0));
					}

					Instruction[] instructions = routine.getInstructions();
					for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements();) {
						BasicBlock bb = (BasicBlock) b.nextElement();
						List<Instruction> instructionList = Arrays.asList(instructions).subList(bb.getStartAddress(),
								bb.getEndAddress() + 1);

						for (Instruction instruction : instructionList) {
							short opcodeType = InstructionTable.InstructionTypeTable[instruction.getOpcode()];
							++localMetrics[opcodeType];
						}
						
						for (int index = 0; index < 2; ++index) {
							if (localMetrics[index] > 0) {
								bb.addBefore(CLASSNAME, "countInstructionOther", new Integer(localMetrics[index]));
								localMetrics[index] = 0;
							}
						}
						
						for (int index = 2; index < 4; ++index) {
							if (localMetrics[index] > 0) {
								bb.addBefore(CLASSNAME, "countInstruction2", new Integer(localMetrics[index]));
								localMetrics[index] = 0;
							}
						}

						for (int index = 4; index < 11; ++index) {
							if (localMetrics[index] > 0) {
								bb.addBefore(CLASSNAME, "countInstruction" + index, new Integer(localMetrics[index]));
								localMetrics[index] = 0;
							}
						}
						
						for (int index = 11; index < 16; ++index) {
							if (localMetrics[index] > 0) {
								bb.addBefore(CLASSNAME, "countInstructionOther", new Integer(localMetrics[index]));
								localMetrics[index] = 0;
							}
						}
					}
				}

				// ci.addAfter(CLASSNAME, "printMetrics", "null");
				File newFile = new File(out_filename);
				newFile.getParentFile().mkdirs();
				ci.write(out_filename);
			}
		}
	}

	public static synchronized void addThreadMetrics(int increment) {
		// _instructionTypeCounter.put(Thread.currentThread().getId(), new
		// Integer[17]);
		Integer[] intergers = new Integer[16];
		for (int index = 0; index < 16; ++index) {
			intergers[index] = new Integer(0);
		}
		_instructionCounter.put(Thread.currentThread().getId(), intergers);
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
	
	public static synchronized void countInstruction6(int increment) {
		Integer[] integers = _instructionCounter.get(Thread.currentThread().getId());
		integers[6] += increment;
		_instructionCounter.put(Thread.currentThread().getId(), integers);
	}
	
	public static synchronized void countInstruction7(int increment) {
		Integer[] integers = _instructionCounter.get(Thread.currentThread().getId());
		integers[7] += increment;
		_instructionCounter.put(Thread.currentThread().getId(), integers);
	}
	
	public static synchronized void countInstruction8(int increment) {
		Integer[] integers = _instructionCounter.get(Thread.currentThread().getId());
		integers[8] += increment;
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
		Integer[] integers = _instructionCounter.get(Thread.currentThread().getId());
		return "Exaustive summary:" + System.lineSeparator() +
			"Instruction Types:" + System.lineSeparator() +
			" STORAGE_INSTRUCTION:          " + integers[2] + System.lineSeparator() +
			" STACK_INSTRUCTION:            " + integers[4] + System.lineSeparator() +
			" ARITHMETIC_INSTRUCTION:       " + integers[5] + System.lineSeparator() +
			" LOGICAL_INSTRUCTION:          " + integers[6] + System.lineSeparator() +
			" CONVERSION_INSTRUCTION:       " + integers[7] + System.lineSeparator() +
			" COMPARISON_INSTRUCTION:       " + integers[8] + System.lineSeparator() +
			" CONDITIONAL_INSTRUCTION:      " + integers[9] + System.lineSeparator() +
			" UNCONDITIONAL_INSTRUCTION:    " + integers[10] + System.lineSeparator() +
			" OTHER:                        " + integers[0] + System.lineSeparator();
	}

	public static void main(String argv[]) {
		argv = new String[3];
		argv[0] = "-metrics";
		argv[1] = "../src";
		argv[2] = "../WebServer";

		if (argv.length < 2 || !argv[0].startsWith("-")) {
			printUsage();
		}

		if (argv[0].equals("-metrics")) {
			if (argv.length != 3) {
				printUsage();
			}

			try {
				File in_dir = new File(argv[1]);
				File out_dir = new File(argv[2]);

				if (in_dir.isDirectory() && out_dir.isDirectory()) {
					doMetrics(in_dir, out_dir);
				} else {
					printUsage();
					printUsage();
				}
			} catch (NullPointerException e) {
				printUsage();
			}
		}

	}
}