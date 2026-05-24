/* (C) Arnaud 'red' Rouyer 2026 */
package ghidra_snes.common;

/**
 * Canonical address window inside each SNES bank.
 *
 * <p>This is intentionally aligned with ROM mirror terminology used by the UI:
 * FULL ($0000-$FFFF), LOW ($0000-$7FFF), HIGH ($8000-$FFFF).
 */
public enum BankWindow {
  FULL(0x0000, 0x10000),
  LOW(0x0000, 0x8000),
  HIGH(0x8000, 0x8000);

  private final int startAddress;
  private final int size;

  BankWindow(int startAddress, int size) {
    this.startAddress = startAddress;
    this.size = size;
  }

  /**
   * Returns the start address within a bank for this window.
   */
  public int startAddress() {
    return startAddress;
  }

  /**
   * Returns the mapped size in bytes for one bank in this window.
   */
  public int size() {
    return size;
  }

  /**
   * Returns the inclusive end address within a bank for this window.
   */
  public int endAddressInclusive() {
    return startAddress + size - 1;
  }
}
