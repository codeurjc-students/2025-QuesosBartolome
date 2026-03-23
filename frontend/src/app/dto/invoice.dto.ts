import { UserBasicDTO } from './userBasic.dto';
import { OrderDTO } from './order.dto';

export interface InvoiceDTO {
  id: number;
  invNo: string;
  user: UserBasicDTO;
  order: OrderDTO;
  taxableBase: number;
  totalPrice: number;
  invoiceDate: string;
}
