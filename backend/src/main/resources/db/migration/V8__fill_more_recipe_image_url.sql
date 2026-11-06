UPDATE recipe
SET image_url = '/images/recipes/' || id || '.webp'
WHERE id IN (
  92, 102, 111, 156, 168, 294, 307, 352, 415, 429, 461, 464, 529, 530
);
