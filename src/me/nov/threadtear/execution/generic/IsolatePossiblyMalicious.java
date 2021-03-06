package me.nov.threadtear.execution.generic;

import java.util.ArrayList;

import org.objectweb.asm.tree.InsnList;

import me.nov.threadtear.asm.Clazz;
import me.nov.threadtear.asm.util.Instructions;
import me.nov.threadtear.execution.Execution;
import me.nov.threadtear.execution.ExecutionCategory;

public class IsolatePossiblyMalicious extends Execution {

	private static final String POSSIBLY_MALICIOUS_REGEX = "(java/lang/runtime/Runtime|java/lang/reflect/).*";
	private int changed;

	public IsolatePossiblyMalicious() {
		super(ExecutionCategory.GENERIC, "Isolate runtime and reflection calls",
				"Isolate runtime and reflection calls, so no code can be executed");
	}

	@Override
	public boolean execute(ArrayList<Clazz> classes, boolean verbose, boolean ignoreErr) {
		this.changed = 0;
		logger.info("Isolating all " + classes.size() + " classes");
		classes.stream().map(c -> c.node).forEach(c -> {
			c.methods.forEach(m -> {
				InsnList newInstructions = Instructions.isolateCallsThatMatch(c, m,
						(s) -> s.matches(POSSIBLY_MALICIOUS_REGEX));
				if (!Instructions.matchOpcodes(m.instructions, newInstructions)) {
					changed++;
					if (verbose) {
						logger.info("Removed calls in " + c.name + "." + m.name + m.desc);
					}
				}
				m.instructions = newInstructions;
			});
		});
		logger.info(changed + " methods containing calls were isolated");
		return changed > 0;
	}
}