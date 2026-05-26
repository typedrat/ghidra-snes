/* (C) Arnaud 'red' Rouyer 2026 */
package ghidra_snes.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RomMapTypeRangesTest {
  @Test
  @DisplayName("LoROM exposes one canonical high-half range")
  void loRomRanges() {
    assertEquals(
      List.of(new BankRange(0x80, 0xbf, BankWindow.HIGH)),
      RomMapType.LoROM.ranges());
  }

  @Test
  @DisplayName("HiROM-like types expose one canonical full-bank range")
  void hiRomLikeRanges() {
    List<BankRange> expected = List.of(new BankRange(0xc0, 0xff, BankWindow.FULL));

    assertEquals(expected, RomMapType.HiROM.ranges());
    assertEquals(expected, RomMapType.SA_1.ranges());
  }

  @Test
  @DisplayName("ExHiROM-like types expose all canonical ranges including $3E-$3F high-half")
  void exHiRomLikeRanges() {
    List<BankRange> expected = List.of(
      new BankRange(0xc0, 0xff, BankWindow.FULL),
      new BankRange(0x40, 0x7f, BankWindow.FULL),
      new BankRange(0x3e, 0x3f, BankWindow.HIGH, 0x8000));

    assertEquals(expected, RomMapType.ExHiROM.ranges());
    assertEquals(expected, RomMapType.SPC7110.ranges());
    assertEquals(expected, RomMapType.S_DD1.ranges());
  }

  @Test
  @DisplayName("Unknown map type has no canonical ranges")
  void unknownHasNoRanges() {
    assertThrows(IllegalStateException.class, () -> RomMapType.UNKNOWN.ranges());
  }
}
