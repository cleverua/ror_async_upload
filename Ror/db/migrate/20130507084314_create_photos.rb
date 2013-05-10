class CreatePhotos < ActiveRecord::Migration
  def change
    create_table :photos do |t|
      t.string :s3_photo
      t.string :fs_photo
      t.string :fs_s3_photo
      t.string :origin_size
      t.integer :origin_height
      t.integer :origin_width

      t.timestamps
    end
  end
end
