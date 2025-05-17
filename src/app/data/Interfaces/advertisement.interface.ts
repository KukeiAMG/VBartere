export interface Advertisement {
  id: number;
  title: string;
  description: string | null;
  imageList: string[] | null;
  subcategory: number | null;
  ownerId: number | null;
  buyersId: number | null;
  status: boolean | null;
}

