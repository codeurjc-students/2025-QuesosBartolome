import { UserBasicDTO } from "./userBasic.dto";
import { CheeseBasicDTO } from "./cheeseBasic.dto";

export interface ReviewDTO {
  id: number;
  rating: number;
  comment: string;
  user: UserBasicDTO;
  cheese: CheeseBasicDTO;
}
