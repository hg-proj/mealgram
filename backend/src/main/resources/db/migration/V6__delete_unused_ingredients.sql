DELETE FROM ingredient i
WHERE NOT EXISTS (SELECT 1 FROM recipe_ingredient ri WHERE ri.ingredient_id = i.id)
  AND NOT EXISTS (SELECT 1 FROM member_ingredient mi WHERE mi.ingredient_id = i.id);
