/* (C) Arnaud 'red' Rouyer 2026 */
package ghidra_snes.common.registers;

import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSpace;
import ghidra.program.model.listing.Program;
import ghidra.program.model.mem.Memory;
import ghidra.program.model.mem.MemoryBlock;
import ghidra.program.model.symbol.SourceType;
import ghidra.program.model.symbol.SymbolTable;
import ghidra_snes.ghidra.MemoryMapUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Vectors {
  private Vectors() {}

  public record VectorRegister(String name, long address) {}

  /**
   * Vector Registers
   */
  public static final List<VectorRegister> VECTOR_REGISTERS = List.of(
      new VectorRegister("VEC_IRQ_EMULATION", 0x00FFFE),
      new VectorRegister("VEC_RESET_EMULATION", 0x00FFFC),
      new VectorRegister("VEC_NMI_EMULATION", 0x00FFFA),
      new VectorRegister("VEC_ABORT_EMULATION", 0x00FFF8),
      new VectorRegister("VEC_COP_EMULATION", 0x00FFF4),
      new VectorRegister("VEC_IRQ_NATIVE", 0x00FFEE),
      new VectorRegister("VEC_NMI_NATIVE", 0x00FFEA),
      new VectorRegister("VEC_ABORT_NATIVE", 0x00FFE8),
      new VectorRegister("VEC_BRK_NATIVE", 0x00FFE6),
      new VectorRegister("VEC_COP_NATIVE", 0x00FFE4)
    );

  /**
   * Creates labels for SNES Vector registers.
   *
   * Labels are only created if they do not already exist. The register
   * definitions are sourced from {@link ghidra_snes.common.registers.Vectors#VECTOR_REGISTERS}.
   *
   * @param program the current program
   * @return number of labels created
   */
  public static int createVectorLabels(Program program) throws Exception {
    int created = 0;
    for (VectorRegister register : Vectors.VECTOR_REGISTERS) {
      created += MemoryMapUtils.ensureLabel(program, register.name(), register.address());
    }

    return created;
  }

  /**
   * Registers CPU vector handlers as program entry points so auto-analysis can
   * seed disassembly without manual intervention.
   *
   * <p>Each vector is a 2-byte bank-$00 pointer; the SNES forces the program
   * bank to $00 on reset/interrupt. We translate the pointer into the canonical
   * ROM bank that actually holds those bytes ({@code canonicalBankBase | target})
   * and mark it as an external entry point, also placing a handler label.
   *
   * <p>Vectors that point below {@code $8000} (unused entries, typically $0000)
   * or whose target is not backed by an initialized block are skipped. Native
   * and emulation vectors that share a target are de-duplicated.
   *
   * @param program the current program
   * @param canonicalBankBase canonical ROM bank base backing CPU bank $00
   *     (see {@link ghidra_snes.ghidra.MemoryMap#canonicalRomBankBase})
   * @return number of entry points created
   */
  public static int createVectorEntryPoints(Program program, long canonicalBankBase)
      throws Exception {
    Memory memory = program.getMemory();
    AddressSpace space = program.getAddressFactory().getDefaultAddressSpace();
    SymbolTable symbols = program.getSymbolTable();
    Set<Long> seeded = new HashSet<>();
    int created = 0;

    for (VectorRegister register : Vectors.VECTOR_REGISTERS) {
      long vectorLocation = canonicalBankBase | (register.address() & 0xffffL);
      Address vectorAddress = space.getAddress(vectorLocation);
      MemoryBlock vectorBlock = memory.getBlock(vectorAddress);
      if (vectorBlock == null || !vectorBlock.isInitialized()) {
        continue;
      }

      int target = (memory.getByte(vectorAddress) & 0xff)
          | ((memory.getByte(vectorAddress.add(1)) & 0xff) << 8);
      if (target < 0x8000) {
        continue;
      }

      long entry = canonicalBankBase | target;
      if (!seeded.add(entry)) {
        continue;
      }

      Address entryAddress = space.getAddress(entry);
      MemoryBlock entryBlock = memory.getBlock(entryAddress);
      if (entryBlock == null || !entryBlock.isInitialized()) {
        continue;
      }

      symbols.addExternalEntryPoint(entryAddress);

      String label = handlerLabel(register.name());
      if (label != null && symbols.getGlobalSymbol(label, entryAddress) == null) {
        symbols.createLabel(entryAddress, label, SourceType.IMPORTED);
      }
      created++;
    }

    return created;
  }

  private static String handlerLabel(String vectorName) {
    if (vectorName.contains("RESET")) {
      return "Reset";
    }
    if (vectorName.contains("NMI")) {
      return "NMI";
    }
    if (vectorName.contains("IRQ")) {
      return "IRQ";
    }
    if (vectorName.contains("BRK")) {
      return "BRK_Handler";
    }
    if (vectorName.contains("COP")) {
      return "COP_Handler";
    }
    if (vectorName.contains("ABORT")) {
      return "ABORT_Handler";
    }
    return null;
  }
}
