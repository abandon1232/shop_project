-- Give the portfolio catalogue a readable public seller name without changing other accounts.
UPDATE business
SET name = 'NorrByte Electronics'
WHERE EXISTS (
    SELECT 1
    FROM goods
    WHERE goods.business_id = business.id
      AND goods.img LIKE '/images/catalog/products/%'
)
  AND (
      TRIM(name) = ''
      OR TRIM(name) REGEXP '^[0-9]+$'
  );
