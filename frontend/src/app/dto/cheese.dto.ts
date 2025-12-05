export interface CheeseDTO {
  id: number;
  name: string;
  price: number;
  description: string;
  manufactureDate: string;
  expirationDate: string;
  type: string;
  boxes: number[];
}