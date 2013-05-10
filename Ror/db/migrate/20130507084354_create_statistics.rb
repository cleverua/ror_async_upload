class CreateStatistics < ActiveRecord::Migration
  def change
    create_table :statistics do |t|
      t.integer :photo_id
      t.decimal :s3_photo_stats, precision: 10, scale: 6
      t.decimal :fs_photo_stats, precision: 10, scale: 6
      t.decimal :fs_s3_photo_stats, precision: 10, scale: 6

      t.timestamps
    end
  end
end
