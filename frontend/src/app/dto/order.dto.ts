import { UserBasicDTO } from './userBasic.dto';
import { OrderItemDTO } from './orderItem.dto';

export interface OrderDTO {
  id: number;
  user: UserBasicDTO;
  totalWeight: number;
  totalPrice: number;
  orderDate: string | null; 
  items: OrderItemDTO[];
}