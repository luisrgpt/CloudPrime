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

abstract class BytecodeAnalyser {
	protected abstract String getClassname();
	protected abstract void initMultiThreadedTable(Routine routine);
	protected abstract void initPrintMetrics(ClassInfo classInfo);
	
	@SuppressWarnings("rawtypes")
	public <T extends Object> void instrumentalizeClass(Class<T> genericClass)
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
			
			initMultiThreadedTable(routine);

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

				for (index = 4; index < 9; ++index) {
					otherAcc += localMetrics[index];
					localMetrics[index] = 0;
				}

				for (index = 9; index < 11; ++index) {
					if (localMetrics[index] > 0) {
						basicBlock.addBefore(getClassname(), "countInstruction" + index, new Integer(localMetrics[index]));
						localMetrics[index] = 0;
					}
				}

				for (index = 11; index < 16; ++index) {
					otherAcc += localMetrics[index];
					localMetrics[index] = 0;
				}

				if (otherAcc > 0) {
					basicBlock.addBefore(getClassname(), "countInstructionOther", new Integer(otherAcc));
					otherAcc = 0;
				}

				if (twoAcc > 0) {
					basicBlock.addBefore(getClassname(), "countInstruction2", new Integer(twoAcc));
					twoAcc = 0;
				}
			}
		}
		initPrintMetrics(classInfo);
		classInfo.write(filename);
		System.out.println(" Done!");
	}
}