#!/usr/bin/djs

Object.prototype.clone = function() { return JSON.parse(JSON.stringify(this)); };
function pick(arr) { return arr[Math.floor(Math.random() * arr.length)]; }

function weathered_brick_models() {
  const model_dir = "src/main/resources/assets/wilescollection/models/block/var";
  const model_pattern = "var_stone_brick_block_model$.json";
  const variant_suffixes = ["0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f"];
  if(!fs.isdir(model_dir)) throw new Error("model path not found: " + model_dir);
  const template = { parent: "minecraft:block/cube", textures: {
    down: "wilescollection:block/var/var_stone_brick_block_texture$",
    up: "wilescollection:block/var/var_stone_brick_block_texture$",
    north: "wilescollection:block/var/var_stone_brick_block_texture$",
    south: "wilescollection:block/var/var_stone_brick_block_texture$",
    west: "wilescollection:block/var/var_stone_brick_block_texture$",
    east: "wilescollection:block/var/var_stone_brick_block_texture$",
    particle: "wilescollection:block/var/var_stone_brick_block_texture0"
  }};
  variant_suffixes.map(function(elem){
    const file_path = model_dir + "/" + model_pattern.replace(/\$/,elem);
    const data = template.clone();
    const sides = Object.keys(data.textures);
    for(var i=0; i<sides.length; ++i) {
      data.textures[sides[i]] = data.textures[sides[i]].replace(/\$/, pick(variant_suffixes));
    }
    fs.writefile(file_path, JSON.stringify(data));
  });
}

//weathered_brick_models();
