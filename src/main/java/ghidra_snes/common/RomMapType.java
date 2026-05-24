/* (C) Arnaud 'red' Rouyer 2026 */
package ghidra_snes.common;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public enum RomMapType {
  LoROM,
  HiROM,
  ExHiROM,
  SA_1,
  SPC7110,
  S_DD1,
  UNKNOWN;

  /**
   * Returns canonical bank ranges for this ROM mapping type.
   */
  public List<BankRange> ranges() {
    return switch (this) {
      case LoROM -> List.of(
        new BankRange(0x80, 0xff, BankWindow.HIGH)
      );
      case HiROM, SA_1 -> List.of(
        new BankRange(0xc0, 0xff, BankWindow.FULL)
      );
      case ExHiROM, SPC7110, S_DD1 -> List.of(
        new BankRange(0xc0, 0xff, BankWindow.FULL),
        new BankRange(0x40, 0x7f, BankWindow.FULL),
        new BankRange(0x3e, 0x3f, BankWindow.HIGH, 0x8000)
      );
      case UNKNOWN -> throw new IllegalStateException("Cannot map unknown ROM type");
    };
  }

  /**
   * Iterates canonical ROM chunks mapped from the input ROM file.
   */
  public Iterable<MappingChunk> mappingChunks(long romSize) {
    BankRange[] mappingRanges = ranges().toArray(BankRange[]::new);
    return () -> new RomMappingIterator(romSize, mappingRanges);
  }

  public record MappingChunk(int bank, long fileOffset, long cpuAddress, long requestedSize) {}

  private static final class RomMappingIterator implements Iterator<MappingChunk> {
    private final long romSize;
    private final BankRange[] bankRanges;

    private int rangeIndex;
    private int bank;
    private long fileOffset;

    private RomMappingIterator(long romSize, BankRange... bankRanges) {
      this.romSize = Math.max(0, romSize);
      this.bankRanges = bankRanges;
      this.rangeIndex = 0;
      this.bank = bankRanges.length == 0 ? 0x100 : bankRanges[0].startBank();
      this.fileOffset = 0;
    }

    @Override
    public boolean hasNext() {
      skipEmptyRanges();
      if (rangeIndex >= bankRanges.length) {
        return false;
      }

      BankRange range = bankRanges[rangeIndex];
      return fileOffset + range.fileSkip() < romSize;
    }

    @Override
    public MappingChunk next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }

      BankRange range = bankRanges[rangeIndex];
      long chunkFileOffset = fileOffset + range.fileSkip();
      int size = (int) Math.min(range.chunkSize(), romSize - chunkFileOffset);
      long memoryOffset = ((long) bank << 16) | (range.window().startAddress() & 0xffff);
      MappingChunk chunk = new MappingChunk(bank, chunkFileOffset, memoryOffset, size);

      fileOffset += range.fileSkip() + size;
      bank++;
      advanceRangeIfNeeded();

      return chunk;
    }

    private void advanceRangeIfNeeded() {
      while (rangeIndex < bankRanges.length && bank > bankRanges[rangeIndex].endBank()) {
        rangeIndex++;
        if (rangeIndex < bankRanges.length) {
          bank = bankRanges[rangeIndex].startBank();
        }
      }
    }

    private void skipEmptyRanges() {
      advanceRangeIfNeeded();
    }
  }
}
