/* (C) Arnaud 'red' Rouyer 2026 */
package ghidra_snes.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BankRangeTest {
  @Test
  @DisplayName("Computes derived values for a high-half bank range")
  void computesDerivedValues() {
    BankRange range = new BankRange(0x80, 0xbf, BankWindow.HIGH);

    assertEquals(64, range.bankCount());
    assertEquals(0x8000, range.chunkSize());
    assertEquals(0xffff, range.addressEndInclusive());
    assertEquals(0, range.fileSkip());
  }

  @Test
  @DisplayName("Rejects invalid bank and skip arguments")
  void rejectsInvalidArguments() {
    assertThrows(IllegalArgumentException.class, () -> new BankRange(-1, 0xbf, BankWindow.HIGH));
    assertThrows(IllegalArgumentException.class, () -> new BankRange(0x80, 0x100, BankWindow.HIGH));
    assertThrows(IllegalArgumentException.class, () -> new BankRange(0x90, 0x8f, BankWindow.HIGH));
    assertThrows(IllegalArgumentException.class, () -> new BankRange(0x80, 0xbf, null));
    assertThrows(IllegalArgumentException.class, () -> new BankRange(0x80, 0xbf, BankWindow.HIGH, -1));
  }
}
