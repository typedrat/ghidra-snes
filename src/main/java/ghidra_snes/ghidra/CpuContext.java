/* (C) Arnaud 'red' Rouyer 2026 */
package ghidra_snes.ghidra;

import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSpace;
import ghidra.program.model.lang.Register;
import ghidra.program.model.listing.Program;
import ghidra.program.util.ProgramContext;
import java.math.BigInteger;

/**
 * Establishes a sane default 65816 register context for freshly imported ROMs.
 *
 * <p>The Data Bank Register ({@code DBR}) and Direct Page register ({@code DP})
 * are ordinary registers rather than SLEIGH context fields, so the disassembler
 * never tracks their values across flow the way it does the m/x/e width context.
 * Left unset they decompile as unknown inputs, producing noise such as
 * {@code (&REG)[(uint3)in_DBR * 0x10000]} for every absolute access and a
 * direct-page-wrap conditional for every {@code <dp} access.
 *
 * <p>The overwhelming majority of SNES code runs with {@code DBR=0} and
 * {@code DP=0} (the CPU reset defaults, which most games never change in their
 * main paths). Asserting that as the program-wide baseline gives clean
 * decompilation out of the box; the genuine exceptions (routines that set DBR/DP
 * to reach far data) are then handled per-function by overriding the value over
 * just that range.
 */
public final class CpuContext {
  private CpuContext() {}

  /**
   * Sets {@code DBR=0} and {@code DP=0} as the default register context across
   * the whole address space.
   *
   * @param program the current program
   */
  public static void applyDefaults(Program program) throws Exception {
    ProgramContext context = program.getProgramContext();
    AddressSpace space = program.getAddressFactory().getDefaultAddressSpace();
    Address start = space.getMinAddress();
    Address end = space.getMaxAddress();

    setRegisterValue(context, "DBR", start, end);
    setRegisterValue(context, "DP", start, end);
  }

  private static void setRegisterValue(
      ProgramContext context, String registerName, Address start, Address end) throws Exception {
    Register register = context.getRegister(registerName);
    if (register != null) {
      context.setValue(register, start, end, BigInteger.ZERO);
    }
  }
}
