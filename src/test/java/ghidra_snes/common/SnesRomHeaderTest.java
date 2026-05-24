/* (C) Arnaud 'red' Rouyer 2026 */
package ghidra_snes.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ghidra_snes.common.cart.CoprocessorType;
import ghidra_snes.common.cart.HardwareFeatures;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SnesRomHeaderTest {
  @Test
  @DisplayName("Zero SRAM size byte means no cartridge RAM")
  void zeroSramSizeByteMeansNoCartridgeRam() {
    SnesRomHeader header = headerWithRamSizeByte(0x00);

    assertEquals(0, header.ramSize());
  }

  @Test
  @DisplayName("Non-zero SRAM size byte decodes as a KiB power of two")
  void nonZeroSramSizeByteDecodesAsKiBPowerOfTwo() {
    assertEquals(8 * 1024, headerWithRamSizeByte(0x03).ramSize());
    assertEquals(32 * 1024, headerWithRamSizeByte(0x05).ramSize());
  }

  @Test
  @DisplayName("titleString trims trailing spaces from ASCII title bytes")
  void titleStringTrimsAsciiTitle() {
    SnesRomHeader header = headerWithTitle("MY GAME TITLE");

    assertEquals("MY GAME TITLE", header.titleString());
  }

  @Test
  @DisplayName("hasPrintableTitle returns false when title contains control bytes")
  void hasPrintableTitleRejectsControlBytes() {
    SnesRomHeader header = headerWithRawTitleByte(0, 0x1f);

    assertFalse(header.hasPrintableTitle());
  }

  @Test
  @DisplayName("hasPrintableTitle returns false when title contains non-ASCII bytes")
  void hasPrintableTitleRejectsBytesAboveAsciiRange() {
    SnesRomHeader header = headerWithRawTitleByte(0, 0x7f);

    assertFalse(header.hasPrintableTitle());
  }

  @Test
  @DisplayName("Unknown mapper nibble decodes as UNKNOWN RomMapType")
  void unknownMapperNibbleDecodesAsUnknownRomMapType() {
    SnesRomHeader header = headerWithMapModeByte(0x24);

    assertEquals(RomMapType.UNKNOWN, header.romMapType());
  }

  @Test
  @DisplayName("cartType-derived accessors expose chipset metadata")
  void cartTypeDerivedAccessorsExposeChipsetMetadata() {
    SnesRomHeader header = headerWithCartBytes(0xf3, 0x03);

    assertEquals(CartType.fromBytes(0xf3, 0x03), header.cartType());
    assertEquals(HardwareFeatures.ROM_COPRO, header.hardwareFeatures());
    assertEquals(CoprocessorType.CX4, header.coprocessorType());
    assertTrue(header.cartType().isKnown());
  }

  private static SnesRomHeader headerWithRamSizeByte(int ramSizeByte) {
    byte[] bytes = emptyHeaderBytes();
    bytes[SnesRomHeader.OFFSET_RAM_SIZE - SnesRomHeader.OFFSET_CUSTOM_CHIP] = (byte) ramSizeByte;
    return new SnesRomHeader(SnesRomHeader.LOROM_HEADER_OFFSET - 1, bytes);
  }

  private static SnesRomHeader headerWithTitle(String title) {
    byte[] bytes = emptyHeaderBytes();
    byte[] titleBytes = String.format("%-21.21s", title).getBytes(StandardCharsets.US_ASCII);
    int offset = SnesRomHeader.OFFSET_CART_TITLE - SnesRomHeader.OFFSET_CUSTOM_CHIP;
    System.arraycopy(titleBytes, 0, bytes, offset, titleBytes.length);
    return new SnesRomHeader(SnesRomHeader.LOROM_HEADER_OFFSET - 1, bytes);
  }

  private static SnesRomHeader headerWithRawTitleByte(int index, int value) {
    byte[] bytes = emptyHeaderBytes();
    int offset = SnesRomHeader.OFFSET_CART_TITLE - SnesRomHeader.OFFSET_CUSTOM_CHIP + index;
    bytes[offset] = (byte) value;
    return new SnesRomHeader(SnesRomHeader.LOROM_HEADER_OFFSET - 1, bytes);
  }

  private static SnesRomHeader headerWithCartBytes(int hardwareByte, int customByte) {
    byte[] bytes = emptyHeaderBytes();
    bytes[SnesRomHeader.OFFSET_CHIPSET - SnesRomHeader.OFFSET_CUSTOM_CHIP] = (byte) hardwareByte;
    bytes[SnesRomHeader.OFFSET_CUSTOM_CHIP - SnesRomHeader.OFFSET_CUSTOM_CHIP] = (byte) customByte;
    return new SnesRomHeader(SnesRomHeader.LOROM_HEADER_OFFSET - 1, bytes);
  }

  private static SnesRomHeader headerWithMapModeByte(int mapModeByte) {
    byte[] bytes = emptyHeaderBytes();
    bytes[SnesRomHeader.OFFSET_ROM_TYPE - SnesRomHeader.OFFSET_CUSTOM_CHIP] = (byte) mapModeByte;
    return new SnesRomHeader(SnesRomHeader.LOROM_HEADER_OFFSET - 1, bytes);
  }

  private static byte[] emptyHeaderBytes() {
    return new byte[Math.toIntExact(SnesRomHeader.HEADER_SIZE)];
  }
}
