export interface OrderItemDTO {
  id: number;
  cheeseId?: number;
  cheeseName: string;
  cheesePrice: number;
  boxes: number[];
  weight: number;
  totalPrice: number;
}
