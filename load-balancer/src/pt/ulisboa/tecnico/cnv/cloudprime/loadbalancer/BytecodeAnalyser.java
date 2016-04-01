package pt.ulisboa.tecnico.cnv.cloudprime.loadbalancer;

import BIT.highBIT.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class BytecodeAnalyser {
	private static int[] _instructionTypeCounter = new int[17],
                         _instructionCounter = new int[256];
	
	private static Stack<int[]> _instructionTypeCounterStack = new Stack<>(),
			                    _instructionCounterStack = new Stack<>();
	
	private static Map<BasicBlock, Integer> _basicBlockTable = new HashMap<>();
	
	private static final String CLASSNAME = "pt/ulisboa/tecnico/cnv/cloudprime/loadbalancer/BytecodeAnalyser";
		
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

	private static List<String> getFileNames(List<String> filenameList, Path parentPath) {
	    try(DirectoryStream<Path> directoryStream = Files.newDirectoryStream(parentPath)) {
	        for (Path path : directoryStream) {
	            if(path.toFile().isDirectory()) {
	                getFileNames(filenameList, path);
	            } else {
	                filenameList.add(path.subpath(1, path.getNameCount()).toString());
	            }
	        }
	    } catch(IOException e) {
	        e.printStackTrace();
	    }
	    return filenameList;
	}
	
	@SuppressWarnings("rawtypes")
	public static void doExaustive(File in_dir, File out_dir) {
		List<String> filelist = getFileNames(new ArrayList<String>(), in_dir.toPath());

		for (String filename : filelist) {
			if (filename.endsWith(".class")) {
				String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
				String out_filename = out_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
				ClassInfo ci = new ClassInfo(in_filename);
	
				for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
					Routine routine = (Routine) e.nextElement();
					
					for (Enumeration instrs = (routine.getInstructionArray()).elements(); instrs.hasMoreElements(); ) {
						Instruction instr = (Instruction) instrs.nextElement();
						int opcode = instr.getOpcode();
						short opcodeType = InstructionTable.InstructionTypeTable[opcode];
						instr.addBefore(CLASSNAME, "countInstruction", new Integer(opcode));
						instr.addBefore(CLASSNAME, "countInstructionType", new Integer(opcodeType));
					}
				}
				ci.addAfter(CLASSNAME, "printExaustive", "null");
				File newFile = new File(out_filename);
				newFile.getParentFile().mkdirs();
				ci.write(out_filename);
			}
		}	
	}
	
	@SuppressWarnings("rawtypes")
	public static void doTest(File in_dir, File out_dir) {
		List<String> filelist = getFileNames(new ArrayList<String>(), in_dir.toPath());
		int numberOfMetrics = 10;
		int[] localMetrics = new int[numberOfMetrics];

		for (String filename : filelist) {
			if (filename.endsWith(".class")) {
				String in_filename = in_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
				String out_filename = out_dir.getAbsolutePath() + System.getProperty("file.separator") + filename;
				ClassInfo ci = new ClassInfo(in_filename);
	
				for (Enumeration e = ci.getRoutines().elements(); e.hasMoreElements(); ) {
                    Routine routine = (Routine) e.nextElement();
                    
					Instruction[] instructions = routine.getInstructions();
                    for (Enumeration b = routine.getBasicBlocks().elements(); b.hasMoreElements(); ) {
                        BasicBlock bb = (BasicBlock) b.nextElement();
                        List<Instruction> instructionList = Arrays.asList(instructions).subList(bb.getStartAddress(), bb.getEndAddress());
                        
                        for (Instruction instruction : instructionList) {
    						//int opcode = instruction.getOpcode();
    						//short opcodeType = InstructionTable.InstructionTypeTable[opcode];
    						++localMetrics[0];
    					}
                        
                        for(int index = 0; index < numberOfMetrics; ++index) {
                        	if(localMetrics[index] > 0) {
                        		bb.addBefore(CLASSNAME, "countInstruction" + index, new Integer(localMetrics[index]));
                        		localMetrics[index] = 0;
                        	}
                        }
                    }
                }

                ci.addAfter(CLASSNAME, "printTest", "null");
                File newFile = new File(out_filename);
				newFile.getParentFile().mkdirs();
				ci.write(out_filename);
			}
		}	
	}
	
	public static synchronized void countInstruction0(int increment) {
		_instructionCounter[0] += increment;
	}
	
	public static synchronized void printTest(String foo) {
		System.out.println("Exaustive summary:" + System.lineSeparator() +
				"Instruction Types:" + System.lineSeparator() +
				" NOP_INSTRUCTION:              " + _instructionTypeCounter[0]);
	}
	
	public static synchronized void printExaustive(String foo) {
		System.out.println("Exaustive summary:" + System.lineSeparator() +
			"Instruction Types:" + System.lineSeparator() +
			" NOP_INSTRUCTION:              " + _instructionTypeCounter[0] + System.lineSeparator() +
			" CONSTANT_INSTRUCTION:         " + _instructionTypeCounter[1] + System.lineSeparator() +
			" LOAD_INSTRUCTION:             " + _instructionTypeCounter[2] + System.lineSeparator() +
			" STORE_INSTRUCTION:            " + _instructionTypeCounter[3] + System.lineSeparator() +
			" STACK_INSTRUCTION:            " + _instructionTypeCounter[4] + System.lineSeparator() +
			" ARITHMETIC_INSTRUCTION:       " + _instructionTypeCounter[5] + System.lineSeparator() +
			" LOGICAL_INSTRUCTION:          " + _instructionTypeCounter[6] + System.lineSeparator() +
			" CONVERSION_INSTRUCTION:       " + _instructionTypeCounter[7] + System.lineSeparator() +
			" COMPARISON_INSTRUCTION:       " + _instructionTypeCounter[8] + System.lineSeparator() +
			" CONDITIONAL_INSTRUCTION:      " + _instructionTypeCounter[9] + System.lineSeparator() +
			" UNCONDITIONAL_INSTRUCTION:    " + _instructionTypeCounter[10] + System.lineSeparator() +
			" CLASS_INSTRUCTION:            " + _instructionTypeCounter[11] + System.lineSeparator() +
			" OBJECT_INSTRUCTION:           " + _instructionTypeCounter[12] + System.lineSeparator() +
			" EXCEPTION_INSTRUCTION:        " + _instructionTypeCounter[13] + System.lineSeparator() +
			" INSTRUCTIONCHECK_INSTRUCTION: " + _instructionTypeCounter[14] + System.lineSeparator() +
			" MONITOR_INSTRUCTION:          " + _instructionTypeCounter[15] + System.lineSeparator() +
			" OTHER_INSTRCTION:             " + _instructionTypeCounter[16] + System.lineSeparator() +
		    "Instructions:" + System.lineSeparator() +
			" aaload:          " + _instructionCounter[50] + System.lineSeparator() +
			" aastore:         " + _instructionCounter[83] + System.lineSeparator() +
			" aconst_null:     " + _instructionCounter[1] + System.lineSeparator() +
			" aload:           " + _instructionCounter[25] + System.lineSeparator() +
			" aload_0:         " + _instructionCounter[42] + System.lineSeparator() +
			" aload_1:         " + _instructionCounter[43] + System.lineSeparator() +
			" aload_2:         " + _instructionCounter[44] + System.lineSeparator() +
			" aload_3:         " + _instructionCounter[45] + System.lineSeparator() +
			" anewarray:       " + _instructionCounter[189] + System.lineSeparator() +
			" areturn:         " + _instructionCounter[176] + System.lineSeparator() +
			" arraylength:     " + _instructionCounter[190] + System.lineSeparator() +
			" astore:          " + _instructionCounter[58] + System.lineSeparator() +
			" astore_0:        " + _instructionCounter[75] + System.lineSeparator() +
			" astore_1:        " + _instructionCounter[76] + System.lineSeparator() +
			" astore_2:        " + _instructionCounter[77] + System.lineSeparator() +
			" astore_3:        " + _instructionCounter[78] + System.lineSeparator() +
			" athrow:          " + _instructionCounter[191] + System.lineSeparator() +
			" baload:          " + _instructionCounter[51] + System.lineSeparator() +
			" bastore:         " + _instructionCounter[84] + System.lineSeparator() +
			" bipush:          " + _instructionCounter[16] + System.lineSeparator() +
			" breakpoint:      " + _instructionCounter[202] + System.lineSeparator() +
			" caload:          " + _instructionCounter[52] + System.lineSeparator() +
			" castore:         " + _instructionCounter[85] + System.lineSeparator() +
			" checkcast:       " + _instructionCounter[192] + System.lineSeparator() +
			" d2f:             " + _instructionCounter[144] + System.lineSeparator() +
			" d2i:             " + _instructionCounter[142] + System.lineSeparator() +
			" d2l:             " + _instructionCounter[143] + System.lineSeparator() +
			" dadd:            " + _instructionCounter[99] + System.lineSeparator() +
			" daload:          " + _instructionCounter[49] + System.lineSeparator() +
			" dastore:         " + _instructionCounter[82] + System.lineSeparator() +
			" dcmpg:           " + _instructionCounter[152] + System.lineSeparator() +
			" dcmpl:           " + _instructionCounter[151] + System.lineSeparator() +
			" dconst_0:        " + _instructionCounter[14] + System.lineSeparator() +
			" dconst_1:        " + _instructionCounter[15] + System.lineSeparator() +
			" ddiv:            " + _instructionCounter[111] + System.lineSeparator() +
			" dload:           " + _instructionCounter[24] + System.lineSeparator() +
			" dload_0:         " + _instructionCounter[38] + System.lineSeparator() +
			" dload_1:         " + _instructionCounter[39] + System.lineSeparator() +
			" dload_2:         " + _instructionCounter[40] + System.lineSeparator() +
			" dload_3:         " + _instructionCounter[41] + System.lineSeparator() +
			" dmul:            " + _instructionCounter[107] + System.lineSeparator() +
			" dneg:            " + _instructionCounter[119] + System.lineSeparator() +
			" drem:            " + _instructionCounter[115] + System.lineSeparator() +
			" dreturn:         " + _instructionCounter[175] + System.lineSeparator() +
			" dstore:          " + _instructionCounter[57] + System.lineSeparator() +
			" dstore_0:        " + _instructionCounter[71] + System.lineSeparator() +
			" dstore_1:        " + _instructionCounter[72] + System.lineSeparator() +
			" dstore_2:        " + _instructionCounter[73] + System.lineSeparator() +
			" dstore_3:        " + _instructionCounter[74] + System.lineSeparator() +
			" dsub:            " + _instructionCounter[103] + System.lineSeparator() +
			" dup:             " + _instructionCounter[89] + System.lineSeparator() +
			" dup_x1:          " + _instructionCounter[90] + System.lineSeparator() +
			" dup_x2:          " + _instructionCounter[91] + System.lineSeparator() +
			" dup2:            " + _instructionCounter[92] + System.lineSeparator() +
			" dup2_x1:         " + _instructionCounter[93] + System.lineSeparator() +
			" dup2_x2:         " + _instructionCounter[94] + System.lineSeparator() +
			" f2d:             " + _instructionCounter[141] + System.lineSeparator() +
			" f2i:             " + _instructionCounter[139] + System.lineSeparator() +
			" f2l:             " + _instructionCounter[140] + System.lineSeparator() +
			" fadd:            " + _instructionCounter[98] + System.lineSeparator() +
			" faload:          " + _instructionCounter[48] + System.lineSeparator() +
			" fastore:         " + _instructionCounter[81] + System.lineSeparator() +
			" fcmpg:           " + _instructionCounter[150] + System.lineSeparator() +
			" fcmpl:           " + _instructionCounter[149] + System.lineSeparator() +
			" fconst_0:        " + _instructionCounter[11] + System.lineSeparator() +
			" fconst_1:        " + _instructionCounter[12] + System.lineSeparator() +
			" fconst_2:        " + _instructionCounter[13] + System.lineSeparator() +
			" fdiv:            " + _instructionCounter[110] + System.lineSeparator() +
			" fload:           " + _instructionCounter[23] + System.lineSeparator() +
			" fload_0:         " + _instructionCounter[34] + System.lineSeparator() +
			" fload_1:         " + _instructionCounter[35] + System.lineSeparator() +
			" fload_2:         " + _instructionCounter[36] + System.lineSeparator() +
			" fload_3:         " + _instructionCounter[37] + System.lineSeparator() +
			" fmul:            " + _instructionCounter[106] + System.lineSeparator() +
			" fneg:            " + _instructionCounter[118] + System.lineSeparator() +
			" frem:            " + _instructionCounter[114] + System.lineSeparator() +
			" freturn:         " + _instructionCounter[174] + System.lineSeparator() +
			" fstore:          " + _instructionCounter[56] + System.lineSeparator() +
			" fstore_0:        " + _instructionCounter[67] + System.lineSeparator() +
			" fstore_1:        " + _instructionCounter[68] + System.lineSeparator() +
			" fstore_2:        " + _instructionCounter[69] + System.lineSeparator() +
			" fstore_3:        " + _instructionCounter[70] + System.lineSeparator() +
			" fsub:            " + _instructionCounter[102] + System.lineSeparator() +
			" getfield:        " + _instructionCounter[180] + System.lineSeparator() +
			" getstatic:       " + _instructionCounter[178] + System.lineSeparator() +
			" GOTO:            " + _instructionCounter[167] + System.lineSeparator() +
			" goto_w:          " + _instructionCounter[200] + System.lineSeparator() +
			" i2b:             " + _instructionCounter[145] + System.lineSeparator() +
			" i2c:             " + _instructionCounter[146] + System.lineSeparator() +
			" i2d:             " + _instructionCounter[135] + System.lineSeparator() +
			" i2f:             " + _instructionCounter[134] + System.lineSeparator() +
			" i2l:             " + _instructionCounter[133] + System.lineSeparator() +
			" i2s:             " + _instructionCounter[147] + System.lineSeparator() +
			" iadd:            " + _instructionCounter[96] + System.lineSeparator() +
			" iaload:          " + _instructionCounter[46] + System.lineSeparator() +
			" iand:            " + _instructionCounter[126] + System.lineSeparator() +
			" iastore:         " + _instructionCounter[79] + System.lineSeparator() +
			" iconst_m1:       " + _instructionCounter[2] + System.lineSeparator() +
			" iconst_0:        " + _instructionCounter[3] + System.lineSeparator() +
			" iconst_1:        " + _instructionCounter[4] + System.lineSeparator() +
			" iconst_2:        " + _instructionCounter[5] + System.lineSeparator() +
			" iconst_3:        " + _instructionCounter[6] + System.lineSeparator() +
			" iconst_4:        " + _instructionCounter[7] + System.lineSeparator() +
			" iconst_5:        " + _instructionCounter[8] + System.lineSeparator() +
			" idiv:            " + _instructionCounter[108] + System.lineSeparator() +
			" if_acmpeq:       " + _instructionCounter[165] + System.lineSeparator() +
			" if_acmpne:       " + _instructionCounter[166] + System.lineSeparator() +
			" if_icmpeq:       " + _instructionCounter[159] + System.lineSeparator() +
			" if_icmpne:       " + _instructionCounter[160] + System.lineSeparator() +
			" if_icmplt:       " + _instructionCounter[161] + System.lineSeparator() +
			" if_icmpge:       " + _instructionCounter[162] + System.lineSeparator() +
			" if_icmpgt:       " + _instructionCounter[163] + System.lineSeparator() +
			" if_icmple:       " + _instructionCounter[164] + System.lineSeparator() +
			" ifeq:            " + _instructionCounter[153] + System.lineSeparator() +
			" ifne:            " + _instructionCounter[154] + System.lineSeparator() +
			" iflt:            " + _instructionCounter[155] + System.lineSeparator() +
			" ifge:            " + _instructionCounter[156] + System.lineSeparator() +
			" ifgt:            " + _instructionCounter[157] + System.lineSeparator() +
			" ifle:            " + _instructionCounter[158] + System.lineSeparator() +
			" ifnonnull:       " + _instructionCounter[199] + System.lineSeparator() +
			" ifnull:          " + _instructionCounter[198] + System.lineSeparator() +
			" iinc:            " + _instructionCounter[132] + System.lineSeparator() +
			" iload:           " + _instructionCounter[21] + System.lineSeparator() +
			" iload_0:         " + _instructionCounter[26] + System.lineSeparator() +
			" iload_1:         " + _instructionCounter[27] + System.lineSeparator() +
			" iload_2:         " + _instructionCounter[28] + System.lineSeparator() +
			" iload_3:         " + _instructionCounter[29] + System.lineSeparator() +
			" impdep1:         " + _instructionCounter[254] + System.lineSeparator() +
			" impdep2:         " + _instructionCounter[255] + System.lineSeparator() +
			" imul:            " + _instructionCounter[104] + System.lineSeparator() +
			" ineg:            " + _instructionCounter[116] + System.lineSeparator() +
			" INSTANCEOF:      " + _instructionCounter[193] + System.lineSeparator() +
			" invokeinterface: " + _instructionCounter[185] + System.lineSeparator() +
			" invokespecial:   " + _instructionCounter[183] + System.lineSeparator() +
			" invokestatic:    " + _instructionCounter[184] + System.lineSeparator() +
			" invokevirtual:   " + _instructionCounter[182] + System.lineSeparator() +
			" ior:             " + _instructionCounter[128] + System.lineSeparator() +
			" irem:            " + _instructionCounter[112] + System.lineSeparator() +
			" ireturn:         " + _instructionCounter[172] + System.lineSeparator() +
			" ishl:            " + _instructionCounter[120] + System.lineSeparator() +
			" ishr:            " + _instructionCounter[122] + System.lineSeparator() +
			" istore:          " + _instructionCounter[54] + System.lineSeparator() +
			" istore_0:        " + _instructionCounter[59] + System.lineSeparator() +
			" istore_1:        " + _instructionCounter[60] + System.lineSeparator() +
			" istore_2:        " + _instructionCounter[61] + System.lineSeparator() +
			" istore_3:        " + _instructionCounter[62] + System.lineSeparator() +
			" isub:            " + _instructionCounter[100] + System.lineSeparator() +
			" iushr:           " + _instructionCounter[124] + System.lineSeparator() +
			" ixor:            " + _instructionCounter[130] + System.lineSeparator() +
			" jsr:             " + _instructionCounter[168] + System.lineSeparator() +
			" jsr_w:           " + _instructionCounter[201] + System.lineSeparator() +
			" l2d:             " + _instructionCounter[138] + System.lineSeparator() +
			" l2f:             " + _instructionCounter[137] + System.lineSeparator() +
			" l2i:             " + _instructionCounter[136] + System.lineSeparator() +
			" ladd:            " + _instructionCounter[97] + System.lineSeparator() +
			" laload:          " + _instructionCounter[47] + System.lineSeparator() +
			" land:            " + _instructionCounter[127] + System.lineSeparator() +
			" lastore:         " + _instructionCounter[80] + System.lineSeparator() +
			" lcmp:            " + _instructionCounter[148] + System.lineSeparator() +
			" lconst_0:        " + _instructionCounter[9] + System.lineSeparator() +
			" lconst_1:        " + _instructionCounter[10] + System.lineSeparator() +
			" ldc:             " + _instructionCounter[18] + System.lineSeparator() +
			" ldc_w:           " + _instructionCounter[19] + System.lineSeparator() +
			" ldc2_w:          " + _instructionCounter[20] + System.lineSeparator() +
			" ldiv:            " + _instructionCounter[109] + System.lineSeparator() +
			" lload:           " + _instructionCounter[22] + System.lineSeparator() +
			" lload_0:         " + _instructionCounter[30] + System.lineSeparator() +
			" lload_1:         " + _instructionCounter[31] + System.lineSeparator() +
			" lload_2:         " + _instructionCounter[32] + System.lineSeparator() +
			" lload_3:         " + _instructionCounter[33] + System.lineSeparator() +
			" lmul:            " + _instructionCounter[105] + System.lineSeparator() +
			" lneg:            " + _instructionCounter[117] + System.lineSeparator() +
			" lookupswitch:    " + _instructionCounter[171] + System.lineSeparator() +
			" lor:             " + _instructionCounter[129] + System.lineSeparator() +
			" lrem:            " + _instructionCounter[113] + System.lineSeparator() +
			" lreturn:         " + _instructionCounter[173] + System.lineSeparator() +
			" lshl:            " + _instructionCounter[121] + System.lineSeparator() +
			" lshr:            " + _instructionCounter[123] + System.lineSeparator() +
			" lstore:          " + _instructionCounter[55] + System.lineSeparator() +
			" lstore_0:        " + _instructionCounter[63] + System.lineSeparator() +
			" lstore_1:        " + _instructionCounter[64] + System.lineSeparator() +
			" lstore_2:        " + _instructionCounter[65] + System.lineSeparator() +
			" lstore_3:        " + _instructionCounter[66] + System.lineSeparator() +
			" lsub:            " + _instructionCounter[101] + System.lineSeparator() +
			" lushr:           " + _instructionCounter[125] + System.lineSeparator() +
			" lxor:            " + _instructionCounter[131] + System.lineSeparator() +
			" monitorenter:    " + _instructionCounter[194] + System.lineSeparator() +
			" monitorexit:     " + _instructionCounter[195] + System.lineSeparator() +
			" multianewarray:  " + _instructionCounter[197] + System.lineSeparator() +
			" NEW:             " + _instructionCounter[187] + System.lineSeparator() +
			" newarray:        " + _instructionCounter[188] + System.lineSeparator() +
			" nop:             " + _instructionCounter[0] + System.lineSeparator() +
			" pop:             " + _instructionCounter[87] + System.lineSeparator() +
			" pop2:            " + _instructionCounter[88] + System.lineSeparator() +
			" putfield:        " + _instructionCounter[181] + System.lineSeparator() +
			" putstatic:       " + _instructionCounter[179] + System.lineSeparator() +
			" ret:             " + _instructionCounter[169] + System.lineSeparator() +
			" RETURN:          " + _instructionCounter[177] + System.lineSeparator() +
			" saload:          " + _instructionCounter[53] + System.lineSeparator() +
			" sastore:         " + _instructionCounter[86] + System.lineSeparator() +
			" sipush:          " + _instructionCounter[17] + System.lineSeparator() +
			" swap:            " + _instructionCounter[95] + System.lineSeparator() +
			" tableswitch:     " + _instructionCounter[170] + System.lineSeparator() +
			" wide:            " + _instructionCounter[196]);
	}
	
	public static synchronized void countInstructionType(int instructionType) {
		if(instructionType == -1) {
		_instructionTypeCounter[16]++;
		} else {
		_instructionTypeCounter[instructionType]++;
		}
	}
	
	public static synchronized void countInstruction(int instruction) {
		_instructionCounter[instruction]++;
	}
		
	public static void main(String argv[]) {
		argv = new String[3];
		argv[0] = "-test";
		argv[1] = "input";
		argv[2] = "bin";
	
		if (argv.length < 2 || !argv[0].startsWith("-")) {
			printUsage();
		}

		if (argv[0].equals("-test")) {
			if (argv.length != 3) {
				printUsage();
			}
			
			try {
				File in_dir = new File(argv[1]);
				File out_dir = new File(argv[2]);

				if (in_dir.isDirectory() && out_dir.isDirectory()) {
					doTest(in_dir, out_dir);
				}
				else {
					printUsage();
				}
			}
			catch (NullPointerException e) {
				printUsage();
			}
		}
		
		if (argv[0].equals("-exaustive")) {
			if (argv.length != 3) {
				printUsage();
			}
			
			try {
				File in_dir = new File(argv[1]);
				File out_dir = new File(argv[2]);

				if (in_dir.isDirectory() && out_dir.isDirectory()) {
					doExaustive(in_dir, out_dir);
				}
				else {
					printUsage();
				}
			}
			catch (NullPointerException e) {
				printUsage();
			}
		}
	}
}
