// Money is Georgian Lari. We label it "GEL" rather than the ₾ sign (U+20BE):
// that glyph is missing from many fonts and renders as a box or a euro-like
// shape, so the ISO code is unambiguous and renders everywhere.
export function formatGel(amount: number): string {
  return new Intl.NumberFormat("en-US").format(amount);
}

export const GEL = "GEL";
