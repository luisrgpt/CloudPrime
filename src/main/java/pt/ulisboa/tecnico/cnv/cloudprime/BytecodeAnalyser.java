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
	private static Map<Long, Long> _instructionTypeCounter = new HashMap<>();
	private static Map<Long, String> _values = new HashMap<>();

	private static final String CLASSNAME = BytecodeAnalyser.class.getName().replace(".", "/");

	static void setValue(String value) {
		_values.put(Thread.currentThread().getId(), value);
	}

	@SuppressWarnings("rawtypes")
	public static void main(String[] args) {
		System.out.println("Instrumenting class...");

		int numberOfMetrics = 16, otherParameterAccumulator = 0, memoryParameterAccumulator = 0,
				conditionalParameterAccumulator = 0, inconditionalParameterAccumulator = 0, index;
		int[] localMetrics = new int[numberOfMetrics];

		String filename = "target/classes/" + IntFactorization.class.getName().replace(".", "/") + ".class";
		if (new File(filename).isFile()) {
			ClassInfo classInfo = new ClassInfo(filename);

			for (Enumeration enumeration = classInfo.getRoutines().elements(); enumeration.hasMoreElements();) {
				Routine routine = (Routine) enumeration.nextElement();

				if (routine.getMethodName().equals("getResponse")) {
					routine.addAfter(CLASSNAME, "storeMetric", new Integer(1));
					routine.addBefore(CLASSNAME, "addThreadMetric", new Integer(0));
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
						otherParameterAccumulator += localMetrics[index];
						localMetrics[index] = 0;
					}

					for (index = 2; index < 4; ++index) {
						memoryParameterAccumulator += localMetrics[index];
						localMetrics[index] = 0;
					}

					for (index = 4; index < 9; ++index) {
						otherParameterAccumulator += localMetrics[index];
						localMetrics[index] = 0;
					}

					if (localMetrics[9] > 0) {
						conditionalParameterAccumulator += localMetrics[9];
						localMetrics[9] = 0;
					}

					if (localMetrics[10] > 0) {
						inconditionalParameterAccumulator += localMetrics[10];
						localMetrics[10] = 0;
					}

					for (index = 11; index < 16; ++index) {
						otherParameterAccumulator += localMetrics[index];
						localMetrics[index] = 0;
					}

					basicBlock.addBefore(CLASSNAME, "incrementMetric",
							new Integer(memoryParameterAccumulator + conditionalParameterAccumulator * 10
									+ inconditionalParameterAccumulator + otherParameterAccumulator));

					memoryParameterAccumulator = 0;
					conditionalParameterAccumulator = 0;
					inconditionalParameterAccumulator = 0;
					otherParameterAccumulator = 0;
				}
			}
			File newFile = new File(filename);
			newFile.getParentFile().mkdirs();
			classInfo.write(filename);
		}

		System.out.println("Completed.");
	}

	public static synchronized void addThreadMetric(int increment) {
		_instructionTypeCounter.put(Thread.currentThread().getId(), new Long(Long.MIN_VALUE));
		new TimerThread(Thread.currentThread().getId()).run();
	}
	
	private static class TimerThread implements Runnable {
		
		private Long _id;
		
		TimerThread(Long id) {
			_id = id;
		}
		
		@Override
		public void run() {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			try {
				DynamoDB.addMetric(_values.get(_id), _instructionTypeCounter.get(_id), false);
			} catch (Exception e) {
				System.out.println("Value " + _values.get(_id) + " already final.");
				return;
			}
			
			new TimerThread(_id).run();
		}
	}

	public static synchronized void incrementMetric(int increment) {
		Long instructionTypeCounter = _instructionTypeCounter.get(Thread.currentThread().getId());
		instructionTypeCounter += increment;
		_instructionTypeCounter.put(Thread.currentThread().getId(), instructionTypeCounter);
	}
	
	private static synchronized void storeMetric(Integer isFinal) {
		new DynamoDBThread(
				_values.get(Thread.currentThread().getId()),
				_instructionTypeCounter.get(Thread.currentThread().getId()),
				isFinal).run();
	}
	
	private static class DynamoDBThread implements Runnable {

		private String _value;
		private Long _metric;
		private Integer _isFinal;
		
		DynamoDBThread(String value, Long metric, Integer isFinal) {
			_value = value;
			_metric = metric;
			_isFinal = isFinal;
		}
		
		@Override
		public void run() {
			try {
				DynamoDB.addMetric(_value, _metric, (_isFinal == 1));
			} catch (Exception e1) {
				System.out.println("Value " + _value + " already final.");
				return;
			}
		}
	}
}