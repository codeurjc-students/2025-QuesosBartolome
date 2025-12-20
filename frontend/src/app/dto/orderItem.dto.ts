import { CheeseBasicDTO } from './cheeseBasic.dto';

export interface OrderItemDTO {
  id: number;
  cheese: CheeseBasicDTO;
  weight: number;
  price: number;
}
