import { UserBasicDTO } from './userBasic.dto';
import { OrderItemDTO } from './orderItem.dto';
    
    export interface CartDTO {
      id: number;
      user: UserBasicDTO;
      totalWeight: number;
      totalPrice: number;
      items: OrderItemDTO[];
    }