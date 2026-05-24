/* (C) Arnaud 'red' Rouyer 2026 */
package ghidra_snes.common.cart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ghidra_snes.common.BankRange;
import ghidra_snes.common.BankWindow;
import ghidra_snes.common.RomMapType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RomMirrorPolicyTest {
  @Test
  @DisplayName("Reserved targets are fixed low system windows")
  void reservedTargetsAreFixedLowSystemWindows() {
    assertEquals(
        List.of(
            new BankRange(0x00, 0x3f, BankWindow.LOW),
            new BankRange(0x80, 0xbf, BankWindow.LOW)),
        RomMirrorPolicy.reservedTargets());
  }

  @Test
  @DisplayName("LoROM exposes one explicit HIGH-source rule")
  void loRomExposesOneExplicitHighSourceRule() {
    List<RomMirrorPolicy.Rule> rules = RomMirrorPolicy.rulesFor(RomMapType.LoROM);

    assertEquals(1, rules.size());
    assertEquals(new BankRange(0x80, 0xff, BankWindow.HIGH), rules.get(0).canonicalSource());
    assertEquals(BankWindow.HIGH, rules.get(0).sourceWindow());
    assertEquals(
        List.of(
            new BankRange(0x00, 0x3f, BankWindow.HIGH),
            new BankRange(0x40, 0x7d, BankWindow.LOW),
            new BankRange(0x40, 0x7d, BankWindow.HIGH),
            new BankRange(0xc0, 0xff, BankWindow.LOW),
            new BankRange(0xc0, 0xff, BankWindow.HIGH)),
        rules.get(0).allowedTargets());
  }

  @Test
  @DisplayName("HiROM exposes FULL/HIGH/LOW source rules")
  void hiRomExposesFullHighLowSourceRules() {
    List<RomMirrorPolicy.Rule> rules = RomMirrorPolicy.rulesFor(RomMapType.HiROM);

    assertEquals(3, rules.size());
    assertEquals(BankWindow.FULL, rules.get(0).sourceWindow());
    assertEquals(BankWindow.HIGH, rules.get(1).sourceWindow());
    assertEquals(BankWindow.LOW, rules.get(2).sourceWindow());
    assertEquals(
        List.of(
            new BankRange(0x40, 0x7d, BankWindow.FULL),
            new BankRange(0xc0, 0xff, BankWindow.FULL)),
        rules.get(0).allowedTargets());
  }

  @Test
  @DisplayName("ExHiROM exposes expected number of explicit rules")
  void exHiRomExposesExpectedNumberOfRules() {
    assertEquals(7, RomMirrorPolicy.rulesFor(RomMapType.ExHiROM).size());
    assertEquals(7, RomMirrorPolicy.rulesFor(RomMapType.SPC7110).size());
    assertEquals(7, RomMirrorPolicy.rulesFor(RomMapType.S_DD1).size());
  }

  @Test
  @DisplayName("targetsFor returns empty list for unsupported selection")
  void targetsForReturnsEmptyListForUnsupportedSelection() {
    assertEquals(
        List.of(),
        RomMirrorPolicy.targetsFor(
            RomMapType.LoROM,
            new BankRange(0x80, 0xff, BankWindow.HIGH),
            BankWindow.LOW));
  }

  @Test
  @DisplayName("Cannot build rules for UNKNOWN ROM type")
  void cannotBuildRulesForUnknownRomType() {
    assertThrows(IllegalStateException.class, () -> RomMirrorPolicy.rulesFor(RomMapType.UNKNOWN));
  }

  @Test
  @DisplayName("buildPlan cycles source banks when target has more banks")
  void buildPlanCyclesSourceBanksWhenTargetHasMoreBanks() {
    BankRange source = new BankRange(0x3e, 0x3f, BankWindow.HIGH);
    BankRange target = new BankRange(0x00, 0x3f, BankWindow.HIGH);

    List<RomMirrorPolicy.MirrorMapping> plan = RomMirrorPolicy.buildPlan(source, target);

    assertEquals(64, plan.size());
    assertEquals(new RomMirrorPolicy.MirrorMapping(0x3e, 0x00, 0x3e8000L, 0x008000L, 0x8000), plan.get(0));
    assertEquals(new RomMirrorPolicy.MirrorMapping(0x3f, 0x01, 0x3f8000L, 0x018000L, 0x8000), plan.get(1));
    assertEquals(new RomMirrorPolicy.MirrorMapping(0x3f, 0x3f, 0x3f8000L, 0x3f8000L, 0x8000), plan.get(63));
  }

}
