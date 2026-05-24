/* (C) Arnaud 'red' Rouyer 2026 */
package ghidra_snes.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ghidra_snes.testing.ByteProviderFactory;
import ghidra_snes.testing.SnesRomHeaderBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SnesRomHeaderDetectorTest {
  private static final long LOROM_TITLE_OFFSET = SnesRomHeader.LOROM_HEADER_OFFSET;

  @Test
  @DisplayName("Detector rejects headers with unknown ROM mapper types")
  void rejectsUnknownRomMapperType() throws Exception {
    byte[] rom = minimalLoRomFile();
    rom[Math.toIntExact(LOROM_TITLE_OFFSET + 0x15)] = (byte) 0x24;

    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> SnesRomHeaderDetector.autoDetectRomHeader(ByteProviderFactory.namedRom("unknown-map.sfc", rom)));

    assertEquals("No valid SNES ROM header found", exception.getMessage());
  }

  @Test
  @DisplayName("Detector rejects headers whose declared ROM size is smaller than file ROM size")
  void rejectsDeclaredRomSizeSmallerThanFileRomSize() throws Exception {
    byte[] rom = minimalLoRomFile();
    rom[Math.toIntExact(LOROM_TITLE_OFFSET + 0x17)] = (byte) 0x05;

    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> SnesRomHeaderDetector.autoDetectRomHeader(ByteProviderFactory.namedRom("small-declared-size.sfc", rom)));

    assertEquals("No valid SNES ROM header found", exception.getMessage());
  }

  @Test
  @DisplayName("Detector rejects headers with extravagant declared ROM sizes")
  void rejectsExtravagantDeclaredRomSize() throws Exception {
    byte[] rom = minimalLoRomFile();
    rom[Math.toIntExact(LOROM_TITLE_OFFSET + 0x17)] = (byte) 0x17;

    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> SnesRomHeaderDetector.autoDetectRomHeader(ByteProviderFactory.namedRom("huge-declared-size.sfc", rom)));

    assertEquals("No valid SNES ROM header found", exception.getMessage());
  }

  @Test
  @DisplayName("Detector rejects headers with non-printable titles")
  void rejectsNonPrintableTitle() throws Exception {
    byte[] rom = minimalLoRomFile();
    rom[Math.toIntExact(LOROM_TITLE_OFFSET)] = 0x1f;

    IllegalStateException exception = assertThrows(
      IllegalStateException.class,
      () -> SnesRomHeaderDetector.autoDetectRomHeader(ByteProviderFactory.namedRom("non-printable-title.sfc", rom)));

    assertEquals("No valid SNES ROM header found", exception.getMessage());
  }

  private static byte[] minimalLoRomFile() throws Exception {
    byte[] rom = new byte[0x10000];
    SnesRomHeaderBuilder.writeHeaderAtTitleOffset(rom, LOROM_TITLE_OFFSET, "HEADER DETECTOR TEST", 0x20);
    SnesRomHeaderBuilder.finalizeChecksumAtTitleOffsets(rom, LOROM_TITLE_OFFSET);
    return rom;
  }
}
