class AddFilenameAndSizeToPhoto < ActiveRecord::Migration
  def change
    add_column :photos, :filename, :string
    add_column :photos, :size, :string
  end
end