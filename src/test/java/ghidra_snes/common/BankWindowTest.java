/* (C) Arnaud 'red' Rouyer 2026 */
package ghidra_snes.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BankWindowTest {
  @Test
  @DisplayName("FULL covers 64 KiB from $0000")
  void fullWindowValues() {
    assertEquals(0x0000, BankWindow.FULL.startAddress());
    assertEquals(0x10000, BankWindow.FULL.size());
    assertEquals(0xffff, BankWindow.FULL.endAddressInclusive());
  }

  @Test
  @DisplayName("LOW covers 32 KiB from $0000")
  void lowWindowValues() {
    assertEquals(0x0000, BankWindow.LOW.startAddress());
    assertEquals(0x8000, BankWindow.LOW.size());
    assertEquals(0x7fff, BankWindow.LOW.endAddressInclusive());
  }

  @Test
  @DisplayName("HIGH covers 32 KiB from $8000")
  void highWindowValues() {
    assertEquals(0x8000, BankWindow.HIGH.startAddress());
    assertEquals(0x8000, BankWindow.HIGH.size());
    assertEquals(0xffff, BankWindow.HIGH.endAddressInclusive());
  }
}
