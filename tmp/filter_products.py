import json
import os

file_path = r'c:\Users\SHUKLA\StudioProjects\EcoGrocer\app\src\main\assets\products.json'

with open(file_path, 'r', encoding='utf-8') as f:
    products = json.load(f)

# Filter products where image is a valid URL (starts with http)
filtered_products = [p for p in products if p.get('image') and p['image'].startswith('http')]

print(f"Original count: {len(products)}")
print(f"Filtered count: {len(filtered_products)}")

with open(file_path, 'w', encoding='utf-8') as f:
    json.dump(filtered_products, f, indent=2)
