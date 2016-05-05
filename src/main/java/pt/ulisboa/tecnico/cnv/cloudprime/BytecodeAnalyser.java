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
	private static Map<Long, Long[]> _instructionTypeCounter = new HashMap<>();
	private static Map<Long, String> _values = new HashMap<>();

	private static final String CLASSNAME = BytecodeAnalyser.class.getName().replace(".", "/");

	static void setValue(String value) {
		_values.put(Thread.currentThread().getId(), value);
	}

	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {
		System.out.println("Instrumenting class...");
		
		int numberOfMetrics = 16, otherAcc = 0, twoAcc = 0, nineAcc = 0, tenAcc = 0, index;
		int[] localMetrics = new int[numberOfMetrics];

		String filename = "target/classes/" + IntFactorization.class.getName().replace(".", "/") + ".class";
		if (new File(filename).isFile()) {
			ClassInfo classInfo = new ClassInfo(filename);

			for (Enumeration enumeration = classInfo.getRoutines().elements(); enumeration.hasMoreElements();) {
				Routine routine = (Routine) enumeration.nextElement();

				if (routine.getMethodName().equals("getResponse")) {
					routine.addAfter(CLASSNAME, "storeMetrics", new Integer(0));
					routine.addBefore(CLASSNAME, "addThreadMetrics", new Integer(0));
				}

				Instruction[] instructions = routine.getInstructions();
				for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements();) {
					BasicBlock basicBlock = (BasicBlock) b.nextElement();
					List<Instruction> instructionList = Arrays.asList(instructions)
							.subList(basicBlock.getStartAddress(), basicBlock.getEndAddress() + 1);

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

					for (index = 4; index < 9; ++index) {
						otherAcc += localMetrics[index];
						localMetrics[index] = 0;
					}

					if (localMetrics[9] > 0) {
						nineAcc += localMetrics[9];
						localMetrics[9] = 0;
					}

					if (localMetrics[10] > 0) {
						tenAcc += localMetrics[10];
						localMetrics[10] = 0;
					}

					for (index = 11; index < 16; ++index) {
						otherAcc += localMetrics[index];
						localMetrics[index] = 0;
					}

					if (twoAcc < 256 && nineAcc < 256 && tenAcc < 256 && otherAcc < 256) {
						if (twoAcc > 0 || nineAcc > 0 || tenAcc > 0 || otherAcc > 0) {
							basicBlock.addBefore(CLASSNAME, "countInstructions",
									new Integer(twoAcc + (nineAcc << 8) + (tenAcc << 16) + (otherAcc << 24)));
						}
					} else if (twoAcc < 65536 && nineAcc < 65536 && tenAcc < 65536 && otherAcc < 65536) {
						if (twoAcc > 0 || nineAcc > 0) {
							basicBlock.addBefore(CLASSNAME, "countInstructions29",
									new Integer(twoAcc + (nineAcc << 16)));
						}
						if (tenAcc > 0 || otherAcc > 0) {
							basicBlock.addBefore(CLASSNAME, "countInstructions10Other",
									new Integer(tenAcc + (otherAcc << 16)));
						}
					} else {
						if (twoAcc > 0) {
							basicBlock.addBefore(CLASSNAME, "countInstruction2", new Integer(twoAcc));
						}

						if (nineAcc > 0) {
							basicBlock.addBefore(CLASSNAME, "countInstruction9", new Integer(nineAcc));
						}

						if (tenAcc > 0) {
							basicBlock.addBefore(CLASSNAME, "countInstruction10", new Integer(tenAcc));
						}

						if (otherAcc > 0) {
							basicBlock.addBefore(CLASSNAME, "countInstructionOther", new Integer(otherAcc));
						}
					}

					twoAcc = 0;
					nineAcc = 0;
					tenAcc = 0;
					otherAcc = 0;
				}
			}
			File newFile = new File(filename);
			newFile.getParentFile().mkdirs();
			classInfo.write(filename);
		}
		
		System.out.println("Completed.");
	}

	public static synchronized void addThreadMetrics(int increment) {
		Long[] instructionTypeCounter = new Long[4];
		for (int index = 0; index < 4; ++index) {
			instructionTypeCounter[index] = new Long(Long.MIN_VALUE);
		}
		_instructionTypeCounter.put(Thread.currentThread().getId(), instructionTypeCounter);
	}

	public static synchronized void countInstruction2(int increment) {
		Long[] instructionTypeCounter = _instructionTypeCounter.get(Thread.currentThread().getId());
		instructionTypeCounter[0] += increment;
		_instructionTypeCounter.put(Thread.currentThread().getId(), instructionTypeCounter);
	}

	public static synchronized void countInstruction9(int increment) {
		Long[] instructionTypeCounter = _instructionTypeCounter.get(Thread.currentThread().getId());
		instructionTypeCounter[1] += increment;
		_instructionTypeCounter.put(Thread.currentThread().getId(), instructionTypeCounter);
	}

	public static synchronized void countInstruction10(int increment) {
		Long[] instructionTypeCounter = _instructionTypeCounter.get(Thread.currentThread().getId());
		instructionTypeCounter[2] += increment;
		_instructionTypeCounter.put(Thread.currentThread().getId(), instructionTypeCounter);
	}

	public static synchronized void countInstructionOther(int increment) {
		Long[] instructionTypeCounter = _instructionTypeCounter.get(Thread.currentThread().getId());
		instructionTypeCounter[3] += increment;
		_instructionTypeCounter.put(Thread.currentThread().getId(), instructionTypeCounter);
	}

	public static synchronized void countInstructions29(int increment) {
		Long[] instructionTypeCounter = _instructionTypeCounter.get(Thread.currentThread().getId());
		instructionTypeCounter[0] += increment & 255;
		instructionTypeCounter[1] += (increment >>> 16) & 255;
		_instructionTypeCounter.put(Thread.currentThread().getId(), instructionTypeCounter);
	}

	public static synchronized void countInstructions10Other(int increment) {
		Long[] instructionTypeCounter = _instructionTypeCounter.get(Thread.currentThread().getId());
		instructionTypeCounter[2] += increment & 255;
		instructionTypeCounter[3] += (increment >>> 16) & 255;
		_instructionTypeCounter.put(Thread.currentThread().getId(), instructionTypeCounter);
	}

	public static synchronized void countInstructions(int increment) {
		Long[] instructionTypeCounter = _instructionTypeCounter.get(Thread.currentThread().getId());
		instructionTypeCounter[0] += increment & 255;
		instructionTypeCounter[1] += (increment >>> 8) & 255;
		instructionTypeCounter[2] += (increment >>> 16) & 255;
		instructionTypeCounter[3] += (increment >>> 24) & 255;
		_instructionTypeCounter.put(Thread.currentThread().getId(), instructionTypeCounter);
	}

	public static synchronized void storeMetrics(int foo) {
		DynamoDB.addMetrics(_values.get(Thread.currentThread().getId()), _instructionTypeCounter.get(Thread.currentThread().getId()));
	}
}