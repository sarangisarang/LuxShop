// Money is displayed in Georgian Lari (₾) to match the LuxShop design.
export function formatGel(amount: number): string {
  return new Intl.NumberFormat("en-US").format(amount);
}

export const GEL = "₾"; // ₾
